package clt_contract


import com.google.inject.Singleton
import domain.{Amount, Currency, Income, IncomeType, Occurrences}

import scala.util.Try
import implicits.{inssTable, irrfTable}

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
        .subtract(salaryDiscounts.map(_.amount): _*)
        .map(netSalary => Income(
          name = "Salary",
          amount = netSalary,
          incomeType = IncomeType.SALARY,
          occurrences = Occurrences.builder.day(5).allMonths.build,
          discounts = salaryDiscounts
        ))

      val thirteenthSalaryTyrable =
        grossSalaryAmount
          .divide(2)
          .subtract(salaryDiscounts.map(_.amount): _*)
          .map(thirteenthSalary => Income(
            name = "Thirteenth Salary",
            amount = thirteenthSalary,
            incomeType = IncomeType.THIRTEENTH_SALARY,
            occurrences = Occurrences.builder.day(20).month(12).build,
            discounts = salaryDiscounts
          ))

      val thirteenthSalaryAdvance = Income(
        name = "Thirteenth Salary Advance",
        amount = grossSalaryAmount.divide(2),
        incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE,
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
