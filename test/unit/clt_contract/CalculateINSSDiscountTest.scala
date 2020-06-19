package unit.clt_contract

import clt_contract.{CalculateINSSDiscount, INSSTable}
import domain.Currency.BRL
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}
import domain.{Amount, AmountRange}
import org.scalatest.{FlatSpec, Matchers}
import scala.util.Success

class CalculateINSSDiscountTest extends FlatSpec with Matchers  {
  implicit private val inssTable = INSSTable(
   List(
     AmountRange(
       from = Amount(0, BRL),
       to = domain.Amount(104500, BRL),
       percentageFactor = 0.075
     ),
     AmountRange(
       from = domain.Amount(104501, BRL),
       to = domain.Amount(208960, BRL),
       percentageFactor = 0.09
     ),
     AmountRange(
       from = domain.Amount(208961, BRL),
       to = domain.Amount(313440, BRL),
       percentageFactor = 0.12
     ),
     AmountRange(
       from = domain.Amount(313441, BRL),
       to = domain.Amount(610106, BRL),
       percentageFactor = 0.14
     )
   ),
    cap = domain.Amount(610106, BRL)
  )
  val EXPECTED_DISCOUNT_NAME = "INSS"

  behavior of "2020 INSS Salary"
  it should "return 8.18% discount for a R$1903,98 salary" in {
    val grossSalary = domain.Amount(190308, BRL)

    val inssDiscount = CalculateINSSDiscount(grossSalary)

    inssDiscount shouldEqual Success(IncomeDiscountPayload(
      EXPECTED_DISCOUNT_NAME,
      IncomeDiscountType.INSS.toString,
      amount = domain.Amount(15561, BRL),
    ))
  }

  it should "return 11.18% discount for a R$5000,00 salary" in {
    val grossSalary = domain.Amount(500000, BRL)

    val inssDiscount = CalculateINSSDiscount(grossSalary)

    inssDiscount shouldEqual Success(IncomeDiscountPayload(
      EXPECTED_DISCOUNT_NAME,
      IncomeDiscountType.INSS.toString,
      amount = domain.Amount(55895, BRL),
    ))
  }

  it should "return 11.69% discount for a R$10000,00 salary" in {
    val grossSalary = domain.Amount(1000000, BRL)

    val inssDiscount = CalculateINSSDiscount(grossSalary)

    inssDiscount shouldEqual Success(IncomeDiscountPayload(
      EXPECTED_DISCOUNT_NAME,
      IncomeDiscountType.INSS.toString,
      amount = domain.Amount(71310, BRL),
    ))
  }

  it should "return 11.69% discount for a R$13000,00 salary" in {
    val grossSalary = domain.Amount(1300000, BRL)

    val inssDiscount = CalculateINSSDiscount(grossSalary)

    inssDiscount shouldEqual Success(IncomeDiscountPayload(
      EXPECTED_DISCOUNT_NAME,
      IncomeDiscountType.INSS.toString,
      amount = domain.Amount(71310, BRL),
    ))
  }

  it should "return 0% when salary is less or equal 0" in {
    val grossSalary = domain.Amount(0, BRL)

    val inssDiscount = CalculateINSSDiscount(grossSalary)

    inssDiscount shouldEqual Success(IncomeDiscountPayload(
      EXPECTED_DISCOUNT_NAME,
      IncomeDiscountType.INSS.toString,
      amount = domain.Amount.BRL(0),
    ))
  }
}
