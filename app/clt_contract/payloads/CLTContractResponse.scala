package clt_contract.payloads

import clt_contract.CLTContract
import domain.Amount.AmountPayload
import domain.Income.IncomePayload
import play.api.libs.json.{Json, Writes}

case class CLTContractResponse(
  grossSalary: AmountPayload,
  incomes: List[IncomePayload]
)

object CLTContractResponse extends IncomePayload.ReadsAndWrites {
  implicit val cltContractResponseWrites: Writes[CLTContractResponse] = Json.writes[CLTContractResponse]

  implicit def fromCLTContract(cltContract: CLTContract): CLTContractResponse = {
    CLTContractResponse(
      grossSalary = cltContract.grossSalary,
      incomes = cltContract.incomes.map(income => income: IncomePayload)
    )
  }
}