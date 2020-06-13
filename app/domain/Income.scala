package domain

import domain.Amount.AmountPayload
import domain.Income.IncomeType
import domain.Occurrences.OccurrencesPayload
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}

case class Income(
  id: String,
  financialContractId: String,
  name: String,
  amount: Amount,
  incomeType: IncomeType.Value,
  occurrences: Occurrences,
  createdAt: DateTime,
  modifiedAt: DateTime,
) {
  def yearlyGrossAmount = amount * occurrences.months.length
}

object Income {
  case object IncomeType extends Enumeration {
    val SALARY: IncomeType.Value = Value("SALARY")
    val THIRTEENTH_SALARY: IncomeType.Value = Value("THIRTEENTH_SALARY")
    val THIRTEENTH_SALARY_ADVANCE: IncomeType.Value = Value("THIRTEENTH_SALARY_ADVANCE")
    val PROFIT_SHARING: IncomeType.Value = Value("PROFIT_SHARING")
  }

  case class IncomePayload(
    name: String,
    amount: AmountPayload,
    incomeType: String,
    occurrences: OccurrencesPayload,
  )

  object IncomePayload {
    implicit val incomePayloadWrites: Writes[IncomePayload] = Json.writes[IncomePayload]
    implicit val incomePayloadReads: Reads[IncomePayload] = Json.reads[IncomePayload]
  }
}