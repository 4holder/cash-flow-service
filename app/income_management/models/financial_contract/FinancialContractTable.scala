package income_management.models.financial_contract

import java.sql.Timestamp

import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

class FinancialContractTable(tag: Tag) extends Table[FinancialContractDbRow](tag, "financial_contracts") {
  def id = column[String]("id")
  def user_id = column[String]("user_id")
  def name = column[String]("name")
  def contract_type = column[String]("contract_type")
  def company_cnpj = column[String]("company_cnpj")
  def is_active = column[Boolean]("is_active")
  def gross_amount_in_cents = column[Long]("gross_amount_in_cents")
  def currency = column[String]("currency")
  def start_date = column[Timestamp]("start_date")
  def end_date = column[Timestamp]("end_date")
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
