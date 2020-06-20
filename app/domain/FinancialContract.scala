package domain

import domain.FinancialContract.ContractType
import org.joda.time.DateTime

case class FinancialContract(
  id: String,
  user: User,
  name: String,
  contractType: ContractType.Value,
  companyCnpj: Option[String],
  startDate: DateTime,
  endDate: Option[DateTime],
  createdAt: DateTime,
  modifiedAt: DateTime
)

object FinancialContract {
  case object ContractType extends Enumeration {
    val CLT: ContractType.Value = Value("CLT")
  }
}
