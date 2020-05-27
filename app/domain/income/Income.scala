package domain.income

import java.sql.Timestamp

import domain.Amount.AmountPayload
import domain.Occurrences.OccurrencesPayload
import domain.income.IncomeDiscount.IncomeDiscountPayload
import domain.{Amount, Currency, Occurrences}
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

case class Income(
  id: String,
  financialContractId: String,
  name: String,
  amount: Amount,
  incomeType: IncomeType.Value,
  occurrences: Occurrences,
  createdAt: DateTime,
  modifiedAt: DateTime,
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
    trait ReadsAndWrites extends
       IncomeDiscountPayload.ReadsAndWrites
       {
      implicit val incomePayloadWrites: Writes[IncomePayload] = Json.writes[IncomePayload]
      implicit val incomePayloadReads: Reads[IncomePayload] = Json.reads[IncomePayload]
    }
  }

  case class IncomeDbRow(
    id: String,
    financial_contract_id: String,
    name: String,
    value_in_cents: Long,
    currency: String,
    income_type: String,
    occurrences: String,
    created_at: Timestamp,
    modified_at: Timestamp,
    is_active: Boolean = true,
  )

  object IncomeDbRow {
    implicit def toIncome(incomeDbRow: IncomeDbRow): Income = Income(
      id = incomeDbRow.id,
      financialContractId = incomeDbRow.financial_contract_id,
      name = incomeDbRow.name,
      amount = Amount(
        valueInCents = incomeDbRow.value_in_cents,
        currency = Currency.withName(incomeDbRow.currency),
      ),
      incomeType = IncomeType.withName(incomeDbRow.income_type),
      occurrences = Occurrences(incomeDbRow.occurrences).get,
      createdAt = new DateTime(incomeDbRow.created_at),
      modifiedAt = new DateTime(incomeDbRow.modified_at),
    )

    implicit def fromIncome(income: Income): IncomeDbRow = IncomeDbRow(
      id = income.id,
      financial_contract_id = income.financialContractId,
      name = income.name,
      value_in_cents = income.amount.valueInCents,
      currency = income.amount.currency.toString,
      income_type = income.incomeType.toString,
      occurrences = income.occurrences.toString,
      created_at = new Timestamp(income.createdAt.getMillis),
      modified_at = new Timestamp(income.modifiedAt.getMillis),
    )
  }

  class IncomeTable(tag: Tag) extends Table[IncomeDbRow](tag, "incomes") {
    def id = column[String]("id", O.PrimaryKey)
    def financial_contract_id = column[String]("financial_contract_id")
    def name = column[String]("name")
    def value_in_cents = column[Long]("value_in_cents")
    def currency = column[String]("currency")
    def income_type = column[String]("income_type")
    def occurrences = column[String]("occurrences")
    def created_at = column[Timestamp]("created_at")
    def modified_at = column[Timestamp]("modified_at")
    def is_active = column[Boolean]("is_active")

    def * = (
      id,
      financial_contract_id,
      name,
      value_in_cents,
      currency,
      income_type,
      occurrences,
      created_at,
      modified_at,
      is_active,
    ) <> ((IncomeDbRow.apply _).tupled, IncomeDbRow.unapply)
  }
}