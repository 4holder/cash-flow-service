package clt_contract.payloads

import clt_contract.CLTContract
import domain.Amount.AmountPayload
import play.api.libs.json.{Json, Writes}

case class CLTContractResponse(
  grossSalary: AmountPayload,
  incomes: List[IncomeResponse]
)

object CLTContractResponse {
  implicit val cltContractResponseWrites: Writes[CLTContractResponse] = Json.writes[CLTContractResponse]

  def adaptToResponse(cltContract: CLTContract): CLTContractResponse = {
    CLTContractResponse(
      grossSalary = cltContract.grossSalary,
      incomes = cltContract.incomes
    )
  }
}