package income_management.payloads

import income_management.models.financial_contract.FinancialContract
import org.joda.time.DateTime
import play.api.libs.json.{Json, Writes}
import wire.AmountPayload.AmountPayloadImplicits
import wire.{AmountPayload, UserPayload}

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

object FinancialContractResponse extends JodaDateTime with AmountPayloadImplicits {
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
