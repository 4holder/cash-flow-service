package clt_contract.payloads

import play.api.libs.json.{Json, Reads}

case class CalculateCLTContractInput (
  grossSalary: Long,
  dependentQuantities: Int,
  deductionsAmount: Long
)

object CalculateCLTContractInput {
  implicit val reads: Reads[CalculateCLTContractInput] = Json.reads[CalculateCLTContractInput]
}
