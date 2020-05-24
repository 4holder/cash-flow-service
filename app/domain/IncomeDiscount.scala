package domain

import domain.Amount.AmountPayload
import play.api.libs.json.{Json, Reads, Writes}

case class IncomeDiscount(
  name: String,
  discountType: IncomeDiscountType.Value,
  amount: Amount,
  grossAmountAliquot: Double
)

object IncomeDiscount {
  case class IncomeDiscountPayload(
    name: String,
    discountType: String,
    amount: AmountPayload,
    grossAmountAliquot: Double
  )

  object IncomeDiscountPayload {
    trait ReadsAndWrites extends AmountPayload.ReadsAndWrites {
      implicit val incomeDiscountPayloadWrites: Writes[IncomeDiscountPayload] = Json.writes[IncomeDiscountPayload]
      implicit val incomeDiscountPayloadReads: Reads[IncomeDiscountPayload] = Json.reads[IncomeDiscountPayload]
    }

    implicit def fromIncomeDiscount(discount: IncomeDiscount): IncomeDiscountPayload = {
      IncomeDiscountPayload(
        name = discount.name,
        discountType = discount.discountType.toString,
        amount = discount.amount,
        grossAmountAliquot = discount.grossAmountAliquot
      )
    }
  }
}
