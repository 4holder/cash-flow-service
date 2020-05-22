package domain

import org.joda.time.DateTime

case class FinancialContract(
  name: String,
  contractType: ContractType.Value,
  incomes: List[Income],
  grossAmount: Amount,
  companyCnpj: String,
  startDate: DateTime,
  endDate: Option[DateTime],
  createdAt: DateTime,
  modifiedAt: DateTime
)
