package unit.domain

import domain.Amount
import org.junit.runner.notification.Failure
import org.scalatest.{FlatSpec, Matchers}
import domain.Currency.{BRL, USD}
import domain.exceptions.InvalidMonetaryOperation

import scala.util.{Failure, Success}

class AmountTest extends FlatSpec with Matchers {
  behavior of "percentage calculation"
  it should "return 40 when asked for 50% of 80" in {
    val amount = Amount(80, BRL)

    val result = amount percentage 0.50

    result shouldEqual domain.Amount(40, BRL)
  }

  it should "return 55895 when asked for 25% of 500000" in {
    val amount = domain.Amount(500000, BRL)

    val result = amount % 0.25

    result shouldEqual domain.Amount(125000, BRL)
  }

  it should "return 10690693 when asked for 18% of 59392741" in {
    val amount = domain.Amount(59392741, BRL)

    val result = amount.percentage(0.18)

    result shouldEqual domain.Amount(10690693, BRL)
  }

  it should "return 593927 when asked for 1% of 59392741" in {
    val amount = domain.Amount(59392741, BRL)

    val result = amount % 0.01

    result shouldEqual domain.Amount(593927, BRL)
  }

  behavior of "subtraction calculation"
  it should "return 100 when subtract 300 from 400" in {
    val amount = domain.Amount(400, BRL)

    val result = amount subtract domain.Amount(300, BRL)

    result.isSuccess shouldEqual true
    result.get shouldEqual domain.Amount(100, BRL)
  }

  it should "return 2 when subtract 398 from 400" in {
    val amount = domain.Amount(400, BRL)

    val result = amount - domain.Amount(398, BRL)

    result.isSuccess shouldEqual true
    result.get shouldEqual domain.Amount(2, BRL)
  }

  it should "return 1 when subtract 2 and 3 from 6" in {
    val amount = domain.Amount(6, BRL)

    val result = amount.subtract(domain.Amount(2, BRL), domain.Amount(3, BRL))

    result.isSuccess shouldEqual true
    result.get shouldEqual domain.Amount(1, BRL)
  }

  it should "raise InvalidOperation when currency do not match" in {
    val amount = domain.Amount(100, BRL)

    val result = amount - domain.Amount(3, USD)

    result.isFailure shouldEqual true
    result.failed.get shouldBe an[InvalidMonetaryOperation]
  }

  behavior of "multiply calculation"
  it should "return 100 when multiply R$10,00 with 10" in {
    val amount = domain.Amount(10, BRL)

    val result = amount multiply 10

    result shouldEqual domain.Amount(100, BRL)
  }

  it should "return R$30,00 when multiply R$6,00 with 5" in {
    val amount = domain.Amount(600, BRL)

    val result = amount * 5

    result shouldEqual domain.Amount(3000, BRL)
  }

  behavior of "divide calculation"
  it should "return R$5 when divide R$10,00 for 2" in {
    val amount = domain.Amount(1000, BRL)

    val result = amount divide 2

    result shouldEqual domain.Amount(500, BRL)
  }

  it should "return R$12,00 when multiply R$60,00 with 5" in {
    val amount = domain.Amount(6000, BRL)

    val result = amount / 5

    result shouldEqual domain.Amount(1200, BRL)
  }

  it should "return R$6,33 when multiply R$19,00 with 3" in {
    val amount = domain.Amount(1900, BRL)

    val result = amount / 3

    result shouldEqual domain.Amount(633, BRL)
  }

  behavior of "less than comparison"
  it should "be true when comparing 1 with 2" in {
    val amount = domain.Amount(1, BRL)

    amount isLessThan domain.Amount(2, BRL) shouldEqual Success(true)
  }

  it should "be false when comparing 2 with 1" in {
    val amount = domain.Amount(2, BRL)

    amount < domain.Amount(1, BRL) shouldEqual Success(false)
  }

  it should "raise InvalidMonetaryOperation comparing different currencies" in {
    val amount = domain.Amount(2, BRL)

    val failedComparison = amount < domain.Amount(1, USD)

    failedComparison.isFailure shouldEqual true
    failedComparison.failed.get shouldBe an[InvalidMonetaryOperation]
  }

  behavior of "greater than comparison"
  it should "be true when comparing 2 with 1" in {
    val amount = domain.Amount(2, BRL)

    amount isGreaterThan domain.Amount(1, BRL) shouldEqual Success(true)
  }

  it should "be false when comparing 1 with 2" in {
    val amount = domain.Amount(1, BRL)

    amount > domain.Amount(2, BRL) shouldEqual Success(false)
  }

  it should "raise InvalidMonetaryOperation comparing different currencies" in {
    val amount = domain.Amount(2, BRL)

    val failedComparison = amount > domain.Amount(1, USD)

    failedComparison.isFailure shouldEqual true
    failedComparison.failed.get shouldBe an[InvalidMonetaryOperation]
  }

  behavior of "is negative verification"
  it should "be negative when value is R$-10,19" in {
    val amount = domain.Amount(-1019, BRL)

    amount.isNegative shouldEqual true
  }

  it should "not be negative when value is R$10,19" in {
    val amount = domain.Amount(2029, BRL)

    amount.isNegative shouldEqual false
  }

  it should "not be negative when value is R$0,00" in {
    val amount = domain.Amount(0, BRL)

    amount.isNegative shouldEqual false
  }

  behavior of "percents for value in cents"
  it should "be 50% when amount of R$100,00 for R$50,00" in {
    val amount = domain.Amount(10000, BRL)

    amount.percentsFor(5000) shouldEqual 0.5
  }

  it should "be 33,33% when amount of R$100,00 for R$33,33" in {
    val amount = domain.Amount(10000, BRL)

    amount.percentsFor(3333) shouldEqual 0.3333
  }
}
