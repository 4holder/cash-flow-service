package wire

import domain.Amount
import play.api.libs.json.{Json, Writes}

case class AmountPayload(
  valueInCents: Long,
  currency: String
)

object AmountPayload {
  implicit val amountPayload: Writes[AmountPayload] = Json.writes[AmountPayload]

  implicit def fromAmount(amount: Amount): AmountPayload = {
    AmountPayload(
      valueInCents = amount.valueInCents,
      currency = amount.currency.toString
    )
  }
}
