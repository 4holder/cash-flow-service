package wire

import domain.Amount
import play.api.libs.json.{Json, Reads, Writes}

case class AmountPayload(
  valueInCents: Long,
  currency: String
)

object AmountPayload {
  trait AmountPayloadImplicits {
    implicit val amountPayloadWrites: Writes[AmountPayload] = Json.writes[AmountPayload]
    implicit val amountPayloadReads: Reads[AmountPayload] = Json.reads[AmountPayload]
  }

  implicit def fromAmount(amount: Amount): AmountPayload = {
    AmountPayload(
      valueInCents = amount.valueInCents,
      currency = amount.currency.toString
    )
  }
}
