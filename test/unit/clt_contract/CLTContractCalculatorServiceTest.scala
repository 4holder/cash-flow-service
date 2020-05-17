package unit.clt_contract

import clt_contract.{CLTContract, CLTContractCalculatorService}
import domain.{Amount, Income, IncomeDiscount, IncomeDiscountType, IncomeType, Occurrences}
import org.scalatest.{FlatSpec, Matchers}

class CLTContractCalculatorServiceTest extends FlatSpec with Matchers {
  val calculator: CLTContractCalculatorService = new CLTContractCalculatorService

  behavior of "CLT Contract"
  it should """should calculate gross R$2000,00 and
              | net salary is R$1835,67,
              | INSS discount is R$164,33 and
              | IRRS discount is R$0,00 for the salaries""".stripMargin in {

    val grossSalaryInCents = 200000
    val expectedSalaryDiscounts = List(
      IncomeDiscount(
        name = IncomeDiscountType.INSS.toString,
        discountType = IncomeDiscountType.INSS,
        amount = Amount.BRL(16433),
        grossAmountAliquot = 0.0822
      ),
      IncomeDiscount(
        name = IncomeDiscountType.IRRF.toString,
        discountType = IncomeDiscountType.IRRF,
        amount = Amount.ZERO_REAIS,
        grossAmountAliquot = 0.0
      )
    )

    val cltContractTryable = calculator.calculateByGrossSalary(
      grossSalaryInCents = grossSalaryInCents,
      0,
      0
    )

    cltContractTryable.isSuccess shouldEqual true
    val cltContract = cltContractTryable.get

    cltContract shouldEqual CLTContract(
      grossSalary = Amount.BRL(grossSalaryInCents),
      incomes = List(
        Income(
          name = "Salary",
          amount = Amount.BRL(183567),
          incomeType = IncomeType.SALARY,
          occurrences = Occurrences.builder.day(5).allMonths.build,
          discounts = expectedSalaryDiscounts
        ),
        Income(
          name = "Thirteenth Salary",
          amount = Amount.BRL(83567),
          incomeType = IncomeType.THIRTEENTH_SALARY,
          occurrences = Occurrences.builder.day(20).month(12).build,
          discounts = expectedSalaryDiscounts
        ),
        Income(
          name = "Thirteenth Salary Advance",
          amount = Amount.BRL(100000),
          incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE,
          occurrences = Occurrences.builder.day(20).month(11).build,
          discounts = List()
        )
      )
    )
  }

  it should """should calculate gross R$11.000,00 with 1 dependent then
              | net salary is R$8.379,50,
              | INSS discount is R$713,10 and
              | IRRS discount is R$1907,40""".stripMargin in {

    val grossSalaryInCents = 1100000
    val expectedSalaryDiscounts = List(
      IncomeDiscount(
        name = IncomeDiscountType.INSS.toString,
        discountType = IncomeDiscountType.INSS,
        amount = Amount.BRL(71310),
        grossAmountAliquot = 0.0648
      ),
      IncomeDiscount(
        name = IncomeDiscountType.IRRF.toString,
        discountType = IncomeDiscountType.IRRF,
        amount = Amount.BRL(190740),
        grossAmountAliquot = 0.1734
      )
    )

    val cltContractTryable = calculator.calculateByGrossSalary(
      grossSalaryInCents = grossSalaryInCents,
      1,
      0
    )

    cltContractTryable.isSuccess shouldEqual true
    val cltContract = cltContractTryable.get

    cltContract shouldEqual CLTContract(
      grossSalary = Amount.BRL(grossSalaryInCents),
      incomes = List(
        Income(
          name = "Salary",
          amount = Amount.BRL(837950),
          incomeType = IncomeType.SALARY,
          occurrences = Occurrences.builder.day(5).allMonths.build,
          discounts = expectedSalaryDiscounts
        ),
        Income(
          name = "Thirteenth Salary",
          amount = Amount.BRL(287950),
          incomeType = IncomeType.THIRTEENTH_SALARY,
          occurrences = Occurrences.builder.day(20).month(12).build,
          discounts = expectedSalaryDiscounts
        ),
        Income(
          name = "Thirteenth Salary Advance",
          amount = Amount.BRL(550000),
          incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE,
          occurrences = Occurrences.builder.day(20).month(11).build,
          discounts = List()
        )
      )
    )
  }

}
