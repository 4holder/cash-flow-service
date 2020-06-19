package unit.clt_contract

import clt_contract.payloads.IncomeResponse
import clt_contract.{CLTContract, CLTContractCalculatorService}
import domain.Income.IncomeType
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}
import domain.{Amount, Occurrences}
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
      IncomeDiscountPayload(
        name = IncomeDiscountType.INSS.toString,
        discountType = IncomeDiscountType.INSS.toString,
        amount = Amount.BRL(16433),
      ),
      IncomeDiscountPayload(
        name = IncomeDiscountType.IRRF.toString,
        discountType = IncomeDiscountType.IRRF.toString,
        amount = Amount.ZERO_REAIS,
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
        IncomeResponse(
          name = "Salary",
          amount = Amount.BRL(183567),
          incomeType = IncomeType.SALARY.toString,
          occurrences = Occurrences.builder.day(5).allMonths.build,
          discounts = expectedSalaryDiscounts
        ),
        IncomeResponse(
          name = "Thirteenth Salary",
          amount = Amount.BRL(83567),
          incomeType = IncomeType.THIRTEENTH_SALARY.toString,
          occurrences = Occurrences.builder.day(20).month(12).build,
          discounts = expectedSalaryDiscounts
        ),
        IncomeResponse(
          name = "Thirteenth Salary Advance",
          amount = Amount.BRL(100000),
          incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE.toString,
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
      IncomeDiscountPayload(
        name = IncomeDiscountType.INSS.toString,
        discountType = IncomeDiscountType.INSS.toString,
        amount = Amount.BRL(71310),
      ),
      IncomeDiscountPayload(
        name = IncomeDiscountType.IRRF.toString,
        discountType = IncomeDiscountType.IRRF.toString,
        amount = Amount.BRL(190740),
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
        IncomeResponse(
          name = "Salary",
          amount = Amount.BRL(837950),
          incomeType = IncomeType.SALARY.toString,
          occurrences = Occurrences.builder.day(5).allMonths.build,
          discounts = expectedSalaryDiscounts
        ),
        IncomeResponse(
          name = "Thirteenth Salary",
          amount = Amount.BRL(287950),
          incomeType = IncomeType.THIRTEENTH_SALARY.toString,
          occurrences = Occurrences.builder.day(20).month(12).build,
          discounts = expectedSalaryDiscounts
        ),
        IncomeResponse(
          name = "Thirteenth Salary Advance",
          amount = Amount.BRL(550000),
          incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE.toString,
          occurrences = Occurrences.builder.day(20).month(11).build,
          discounts = List()
        )
      )
    )
  }

}
