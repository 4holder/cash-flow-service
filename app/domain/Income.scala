package domain

import domain.Amount.AmountPayload
import domain.IncomeDiscount.IncomeDiscountPayload
import domain.Occurrences.OccurrencesPayload
import play.api.libs.json.{Json, Reads, Writes}

case class Income(
  name: String,
  amount: Amount,
  incomeType: IncomeType.Value,
  occurrences: Occurrences,
  discounts: List[IncomeDiscount]
)

object Income {
  case class IncomePayload(
    name: String,
    amount: AmountPayload,
    incomeType: String,
    occurrences: OccurrencesPayload,
    discounts: List[IncomeDiscountPayload]
  )

  object IncomePayload {
    trait ReadsAndWrites extends AmountPayload.ReadsAndWrites
      with IncomeDiscountPayload.ReadsAndWrites
      with OccurrencesPayload.ReadsAndWrites {
      implicit val incomePayloadWrites: Writes[IncomePayload] = Json.writes[IncomePayload]
      implicit val incomePayloadReads: Reads[IncomePayload] = Json.reads[IncomePayload]
    }

    implicit def fromIncome(income: Income): IncomePayload = {
      IncomePayload(
        name = income.name,
        amount = income.amount,
        incomeType = income.incomeType.toString,
        occurrences = income.occurrences,
        discounts = income.discounts.map(discount => discount: IncomeDiscountPayload)
      )
    }
  }
}