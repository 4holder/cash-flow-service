package domain

import domain.exceptions.InvalidMonetaryOperationException
import play.api.libs.json.{Json, Reads, Writes}
import scala.util.{Failure, Success, Try}

case class Amount(
  valueInCents: Long,
  currency: Currency.Value
) {
  def percentage(factor: Double): Amount = {
    this.copy(math round valueInCents * factor)
  }

  def %(factor: Double): Amount = percentage(factor)

  def subtract(amounts: Amount*): Try[Amount] = {
    if(amounts.forall(_.currency.equals(this.currency))){
      val amountToSubtract = amounts.map(_.valueInCents).sum
      Success(this.copy(valueInCents - amountToSubtract))
    } else {
      Failure(InvalidMonetaryOperationException(
        s"Operation not permitted. Currency mismatch"))
    }
  }

  def -(amounts: Amount): Try[Amount] = subtract(amounts)

  def isLessThan(amount: Amount): Try[Boolean] = {
    if (this.currency.equals(amount.currency)) {
      Success(this.valueInCents < amount.valueInCents)
    } else {
      Failure(InvalidMonetaryOperationException(
        s"Invalid comparison. Currency ${this.currency} mismatch from ${amount.currency}"))
    }
  }

  def < (amount: Amount): Try[Boolean] = isLessThan(amount)

  def isGreaterThan(amount: Amount): Try[Boolean] = {
    if (this.currency.equals(amount.currency)) {
      Success(this.valueInCents > amount.valueInCents)
    } else {
      Failure(InvalidMonetaryOperationException(
        s"Invalid comparison. Currency ${this.currency} mismatch from ${amount.currency}"))
    }
  }

  def isNegative: Boolean = this.valueInCents < 0

  def > (amount: Amount): Try[Boolean] = isGreaterThan(amount)

  def multiply(value: Long): Amount = {
    this.copy(valueInCents * value)
  }

  def *(value: Long): Amount = multiply(value)

  def divide(value: Long): Amount = {
    this.copy(valueInCents / value)
  }

  def /(value: Long): Amount = divide(value)

  def percentsFor(amountInCents: Long): Double = {
    Try(BigDecimal(amountInCents.toDouble / this.valueInCents.toDouble)
      .setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble)
      .getOrElse(0d)
  }
}

object Amount {
  implicit val amountWrites: Writes[Amount] = Json.writes[Amount]
  val ZERO_REAIS = Amount(0, Currency.BRL)

  def BRL(valueInCents: Long) = Amount(valueInCents, Currency.BRL)

  case class AmountPayload(
    valueInCents: Long,
    currency: String
  )

  object AmountPayload {
    implicit val amountPayloadWrites: Writes[AmountPayload] = Json.writes[AmountPayload]
    implicit val amountPayloadReads: Reads[AmountPayload] = Json.reads[AmountPayload]

    implicit def fromAmount(amount: Amount): AmountPayload = {
      AmountPayload(
        valueInCents = amount.valueInCents,
        currency = amount.currency.toString
      )
    }

    implicit def toAmount(amountPayload: AmountPayload): Amount = Amount(
      valueInCents = amountPayload.valueInCents,
      currency = Currency.withName(amountPayload.currency)
    )
  }
}