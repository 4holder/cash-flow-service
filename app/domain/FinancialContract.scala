package domain

import domain.Amount.AmountPayload
import domain.FinancialContract.ContractType
import infrastructure.reads_and_writes.JodaDateTime
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

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
  case object ContractType extends Enumeration {
    val CLT: ContractType.Value = Value("CLT")
  }

  case class FinancialContractPayload(
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
  )

  object FinancialContractPayload extends JodaDateTime {
    implicit val financialContractInput: Reads[FinancialContractPayload] = Json.reads[FinancialContractPayload]
  }
}
