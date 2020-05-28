package clt_contract

import com.google.inject.Singleton
import domain.Income.{IncomePayload, IncomeType}
import domain.{Amount, Currency, Occurrences}
import implicits.{inssTable2020, irrfTable2020}

import scala.util.Try

@Singleton
class CLTContractCalculatorService {
  def calculateByGrossSalary(
    grossSalaryInCents: Long,
    dependentsQuantity: Int,
    deductionsInCents: Long,
    currency: Currency.Value = Currency.BRL
  ): Try[CLTContract] = {
    val grossSalaryAmount = Amount(grossSalaryInCents, currency)
    val deductions = Amount(deductionsInCents, currency)

    val salaryDiscounts = for {
      inssDiscount <- CalculateINSSDiscount(grossSalaryAmount)
      irrfDiscount <- CalculateIRRFDiscount(grossSalaryAmount, inssDiscount, dependentsQuantity, deductions)
    } yield List(inssDiscount, irrfDiscount)

    salaryDiscounts.flatMap { salaryDiscounts =>
      val netSalaryTryable = grossSalaryAmount
        .subtract(salaryDiscounts.map(_.amount : Amount): _*)
        .map(netSalary => IncomePayload(
          name = "Salary",
          amount = netSalary,
          incomeType = IncomeType.SALARY.toString,
          occurrences = Occurrences.builder.day(5).allMonths.build,
          discounts = salaryDiscounts
        ))

      val thirteenthSalaryTyrable =
        grossSalaryAmount
          .divide(2)
          .subtract(salaryDiscounts.map(_.amount : Amount): _*)
          .map(thirteenthSalary => IncomePayload(
            name = "Thirteenth Salary",
            amount = thirteenthSalary,
            incomeType = IncomeType.THIRTEENTH_SALARY.toString,
            occurrences = Occurrences.builder.day(20).month(12).build,
            discounts = salaryDiscounts
          ))

      val thirteenthSalaryAdvance = IncomePayload(
        name = "Thirteenth Salary Advance",
        amount = grossSalaryAmount.divide(2),
        incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE.toString,
        occurrences = Occurrences.builder.day(20).month(11).build,
        discounts = List()
      )

      for {
        netSalaryIncome <- netSalaryTryable
        thirteenthSalary <- thirteenthSalaryTyrable
      } yield CLTContract(
        grossSalary = grossSalaryAmount,
        incomes = List(
          netSalaryIncome,
          thirteenthSalary,
          thirteenthSalaryAdvance
        )
      )
    }
  }
}
