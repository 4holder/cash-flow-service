package domain.financial_contract

import java.sql.Timestamp

import domain.Amount.AmountPayload
import domain.User.UserPayload
import domain.{Amount, Currency, RepositoryModel, User}
import infrastructure.reads_and_writes.JodaDateTime
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

case class FinancialContract(
  id: String,
  user: User,
  name: String,
  contractType: ContractType.Value,
  grossAmount: Amount,
  companyCnpj: Option[String],
  startDate: DateTime,
  endDate: Option[DateTime],
  createdAt: DateTime,
  modifiedAt: DateTime
) extends RepositoryModel

object FinancialContract {
  implicit def fromFinancialContractDbRow(financialContractDbRow: FinancialContractDbRow): FinancialContract = {
    FinancialContract(
      id = financialContractDbRow.id,
      user = User(financialContractDbRow.user_id),
      name = financialContractDbRow.name,
      contractType = ContractType.withName(financialContractDbRow.contract_type),
      companyCnpj = financialContractDbRow.company_cnpj,
      grossAmount = Amount(
        valueInCents = financialContractDbRow.gross_amount_in_cents,
        currency = Currency.withName(financialContractDbRow.currency)
      ),
      startDate = new DateTime(financialContractDbRow.start_date),
      endDate = financialContractDbRow.end_date.map(ed => new DateTime(ed)),
      createdAt = new DateTime(financialContractDbRow.created_at),
      modifiedAt = new DateTime(financialContractDbRow.modified_at)
    )
  }

  case class FinancialContractPayload(
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
  )

  case class FinancialContractUpdate(
    name: String,
    contractType: ContractType.Value,
    grossAmount: Amount,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
  )

  object FinancialContractPayload extends JodaDateTime {
    implicit val financialContractInput: Reads[FinancialContractPayload] = Json.reads[FinancialContractPayload]
  }

  case class FinancialContractResponse(
    id: String,
    user: UserPayload,
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
    createdAt: DateTime,
    modifiedAt: DateTime
  )

  object FinancialContractResponse extends JodaDateTime
    with UserPayload.ReadsAndWrites {
    implicit val financialContractResponse: Writes[FinancialContractResponse] = Json.writes[FinancialContractResponse]

    implicit def fromFinancialContract(financialContract: FinancialContract): FinancialContractResponse = {
      FinancialContractResponse(
        id = financialContract.id,
        user = financialContract.user,
        name = financialContract.name,
        contractType = financialContract.contractType.toString,
        grossAmount = financialContract.grossAmount,
        companyCnpj = financialContract.companyCnpj,
        startDate = financialContract.startDate,
        endDate = financialContract.endDate,
        createdAt = financialContract.createdAt,
        modifiedAt = financialContract.modifiedAt
      )
    }
  }

  case class FinancialContractDbRow(
    id: String,
    user_id: String,
    name: String,
    contract_type: String,
    company_cnpj: Option[String],
    is_active: Boolean,
    gross_amount_in_cents: Long,
    currency: String,
    start_date: Timestamp,
    end_date: Option[Timestamp],
    created_at: Timestamp,
    modified_at: Timestamp
  )

  object FinancialContractDbRow {
    implicit def fromFinancialContract(financialContract: FinancialContract): FinancialContractDbRow = {
      FinancialContractDbRow(
        id = financialContract.id,
        user_id = financialContract.user.id,
        name = financialContract.name,
        contract_type = financialContract.contractType.toString,
        company_cnpj = financialContract.companyCnpj,
        is_active = true,
        gross_amount_in_cents = financialContract.grossAmount.valueInCents,
        currency = financialContract.grossAmount.currency.toString,
        start_date = new Timestamp(financialContract.startDate.getMillis),
        end_date = financialContract.endDate.map(d => new Timestamp(d.getMillis)),
        created_at = new Timestamp(financialContract.createdAt.getMillis),
        modified_at = new Timestamp(financialContract.modifiedAt.getMillis)
      )
    }
  }

  class FinancialContractTable(tag: Tag) extends Table[FinancialContractDbRow](tag, "financial_contracts") {
    def id = column[String]("id", O.PrimaryKey)
    def user_id = column[String]("user_id")
    def name = column[String]("name")
    def contract_type = column[String]("contract_type")
    def company_cnpj = column[Option[String]]("company_cnpj")
    def is_active = column[Boolean]("is_active")
    def gross_amount_in_cents = column[Long]("gross_amount_in_cents")
    def currency = column[String]("currency")
    def start_date = column[Timestamp]("start_date")
    def end_date = column[Option[Timestamp]]("end_date")
    def created_at = column[Timestamp]("created_at")
    def modified_at = column[Timestamp]("modified_at")

    def * = (
      id,
      user_id,
      name,
      contract_type,
      company_cnpj,
      is_active,
      gross_amount_in_cents,
      currency,
      start_date,
      end_date,
      created_at,
      modified_at
    ) <> ((FinancialContractDbRow.apply _).tupled, FinancialContractDbRow.unapply)
  }
}
