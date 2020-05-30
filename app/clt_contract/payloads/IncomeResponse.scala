package clt_contract.payloads

import domain.Amount.AmountPayload
import domain.IncomeDiscount.IncomeDiscountPayload
import domain.Occurrences.OccurrencesPayload
import play.api.libs.json.{Json, Writes}


case class IncomeResponse(
  name: String,
  amount: AmountPayload,
  incomeType: String,
  occurrences: OccurrencesPayload,
  discounts: List[IncomeDiscountPayload]
)

object IncomeResponse extends IncomeDiscountPayload.ReadsAndWrites {
  implicit val incomeResponseWrites: Writes[IncomeResponse] = Json.writes[IncomeResponse]
}
