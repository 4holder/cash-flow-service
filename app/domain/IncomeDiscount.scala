package domain

import domain.Amount.AmountPayload
import domain.IncomeDiscount.IncomeDiscountType
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}

case class IncomeDiscount(
  id: String,
  incomeId: String,
  name: String,
  discountType: IncomeDiscountType.Value,
  amount: Amount,
  createdAt: DateTime,
  modifiedAt: DateTime,
)

object IncomeDiscount {
  case object IncomeDiscountType extends Enumeration {
    val INSS: IncomeDiscountType.Value = Value("INSS")
    val IRRF: IncomeDiscountType.Value = Value("IRRF")
  }

  case class IncomeDiscountPayload(
    name: String,
    discountType: String,
    amount: AmountPayload,
  )

  object IncomeDiscountPayload {
    implicit val incomeDiscountPayloadWrites: Writes[IncomeDiscountPayload] = Json.writes[IncomeDiscountPayload]
    implicit val incomeDiscountPayloadReads: Reads[IncomeDiscountPayload] = Json.reads[IncomeDiscountPayload]

    implicit def fromIncomeDiscount(discount: IncomeDiscount): IncomeDiscountPayload = {
      IncomeDiscountPayload(
        name = discount.name,
        discountType = discount.discountType.toString,
        amount = discount.amount,
      )
    }
  }
}
