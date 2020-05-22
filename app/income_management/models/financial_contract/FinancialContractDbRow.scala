package income_management.models.financial_contract

import java.sql.Timestamp

case class FinancialContractDbRow(
  id: String,
  user_id: String,
  name: String,
  contract_type: String,
  company_cnpj: String,
  is_active: Boolean,
  gross_amount_in_cents: Long,
  currency: String,
  start_date: Timestamp,
  end_date: Timestamp,
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
      company_cnpj = financialContract.companyCnpj.orNull,
      is_active = true,
      gross_amount_in_cents = financialContract.grossAmount.valueInCents,
      currency = financialContract.grossAmount.currency.toString,
      start_date = new Timestamp(financialContract.startDate.getMillis),
      end_date = financialContract.endDate.map(d => new Timestamp(d.getMillis)).orNull,
      created_at = new Timestamp(financialContract.createdAt.getMillis),
      modified_at = new Timestamp(financialContract.modifiedAt.getMillis)
    )
  }
}
