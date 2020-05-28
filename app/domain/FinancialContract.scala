package domain

import domain.Amount.AmountPayload
import domain.FinancialContract.ContractType
import domain.User.UserPayload
import infrastructure.reads_and_writes.JodaDateTime
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads, Writes}


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
}
