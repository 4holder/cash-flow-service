package income_management.models.financial_contract

import java.sql.Timestamp

import domain.{Amount, ContractType, Currency, User}
import org.joda.time.DateTime

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
)

object FinancialContract {
  implicit def fromFinancialContractDbRow(financialContractDbRow: FinancialContractDbRow): FinancialContract = {
    FinancialContract(
      id = financialContractDbRow.id,
      user = User(financialContractDbRow.user_id),
      name = financialContractDbRow.name,
      contractType = ContractType.withName(financialContractDbRow.contract_type),
      companyCnpj = Option(financialContractDbRow.company_cnpj),
      grossAmount = Amount(
        valueInCents = financialContractDbRow.gross_amount_in_cents,
        currency = Currency.withName(financialContractDbRow.currency)
      ),
      startDate = new DateTime(financialContractDbRow.start_date),
      endDate = Option(new DateTime(financialContractDbRow.end_date)),
      createdAt = new DateTime(financialContractDbRow.created_at),
      modifiedAt = new DateTime(financialContractDbRow.modified_at)
    )
  }
}
