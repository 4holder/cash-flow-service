package unit.clt_contract

import clt_contract.{CalculateIRRFDiscount, IRRFTable}
import domain.Currency.BRL
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}
import domain.{Amount, AmountRange}
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class CalculateIRRFDiscountTest extends FlatSpec with Matchers {
  implicit private val irrfTable = IRRFTable(
    ranges = List(
      AmountRange(
        from = Amount(0, BRL),
        to = domain.Amount(190398, BRL),
        percentageFactor = 0
      ),
      AmountRange(
        from = domain.Amount(190399, BRL),
        to = domain.Amount(282665, BRL),
        percentageFactor = 0.075
      ),
      AmountRange(
        from = domain.Amount(282666, BRL),
        to = domain.Amount(375105, BRL),
        percentageFactor = 0.15
      ),
      AmountRange(
        from = domain.Amount(375106, BRL),
        to = domain.Amount(466468, BRL),
        percentageFactor = 0.225
      ),
      AmountRange(
        from = domain.Amount(466469, BRL),
        to = domain.Amount(Long.MaxValue, BRL),
        percentageFactor = 0.275
      )
    ),
    discountPerDependent = domain.Amount(18959, BRL)
  )
  val EXPECTED_DISCOUNT_NAME = "IRRF"

  behavior of "salary without IRRF discount"
  it should "not have discount when salary is less than the minimal" in {
    val inssDiscount = IncomeDiscountPayload(
      "INSS",
      IncomeDiscountType.INSS.toString,
      domain.Amount(15569, BRL),
      0.0818
    )
    val grossSalary = domain.Amount(190398, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      0,
      otherDeductions = domain.Amount(0, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(0, BRL),
        0
      )
    )
  }

  behavior of "salary with IRRF discount"
  it should "should return 0.05% discount for a value in the beginning of first range" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(17240, BRL),
      0.0825
    )
    val grossSalary = domain.Amount(208967, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      0,
      otherDeductions = domain.Amount(0, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(100, BRL),
        0.0005
      )
    )
  }

  it should "return 3,44% discount for a value in the middle of the third range" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(35269, BRL),
      0.1000
    )
    val grossSalary = domain.Amount(352665, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      0,
      otherDeductions = domain.Amount(0, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(12130, BRL),
        0.0344
      )
    )
  }

  it should "return 9.33% discount for a value greater than the last range" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(67095, BRL),
      0.1157
    )
    val grossSalary = domain.Amount(580000, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      0,
      otherDeductions = domain.Amount(0, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(54113, BRL),
        0.0933
      )
    )
  }

  it should "should return 17,34% discount for 11k with 1 dependent" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(71308, BRL),
      0.0648
    )
    val grossSalary = domain.Amount(1100000, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      1,
      otherDeductions = domain.Amount(0, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(190741, BRL),
        0.1734
      )
    )
  }

  it should "should return 16,87% discount for 11k with 2 dependent" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(71308, BRL),
      0.0648
    )
    val grossSalary = domain.Amount(1100000, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      2,
      otherDeductions = domain.Amount(0, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(185527, BRL),
        0.1687
      )
    )
  }

  it should "return 15,31% discount for 11k with 0 dependent and R$1000 of deductions" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(71308, BRL),
      0.0648
    )
    val grossSalary = domain.Amount(1100000, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      0,
      otherDeductions = domain.Amount(100000, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(168455, BRL),
        0.1531
      )
    )
  }

  it should "return 22,88% discount for 35k with 0 dependent and R$2000 of deductions" in {
    val inssDiscount = IncomeDiscountPayload(
      IncomeDiscountType.INSS.toString,
      IncomeDiscountType.INSS.toString,
      domain.Amount(71308, BRL),
      0.0648
    )
    val grossSalary = domain.Amount(3500000, BRL)

    val irrfDiscount = CalculateIRRFDiscount(
      grossSalary,
      inssDiscount,
      0,
      otherDeductions = domain.Amount(200000, BRL)
    )

    irrfDiscount shouldEqual Success(
      IncomeDiscountPayload(
        EXPECTED_DISCOUNT_NAME,
        IncomeDiscountType.IRRF.toString,
        domain.Amount(800955, BRL),
        0.2288
      )
    )
  }
}
