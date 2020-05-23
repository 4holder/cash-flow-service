package income_management.payloads

import income_management.models.financial_contract.FinancialContract
import org.joda.time.DateTime
import play.api.libs.json.{JodaWrites, Json, Writes}
import wire.{AmountPayload, UserPayload}

case class FinancialContractResponse(
  id: String,
  user: UserPayload,
  name: String,
  contractType: String,
  grossAmount: AmountPayload,
  companyCnpj: String,
  startDate: DateTime,
  endDate: DateTime,
  createdAt: DateTime,
  modifiedAt: DateTime
)

object FinancialContractResponse {
  val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")
  implicit val financialContractResponse: Writes[FinancialContractResponse] = Json.writes[FinancialContractResponse]

  implicit def fromFinancialContract(financialContract: FinancialContract): FinancialContractResponse = {
    FinancialContractResponse(
      id = financialContract.id,
      user = financialContract.user,
      name = financialContract.name,
      contractType = financialContract.contractType.toString,
      grossAmount = financialContract.grossAmount,
      companyCnpj = financialContract.companyCnpj.orNull,
      startDate = financialContract.startDate,
      endDate = financialContract.endDate.orNull,
      createdAt = financialContract.createdAt,
      modifiedAt = financialContract.modifiedAt
    )
  }
}
