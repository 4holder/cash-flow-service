package utils.builders

import domain.Amount
import domain.FinancialContract.ContractType
import income_management.controllers.FinancialContractController.FinancialContractUpdateInput
import org.joda.time.DateTime

case class FinancialContractUpdateInputBuilder(
  name: String = "A Updated Good Contract Payload",
  contractType: ContractType.Value = ContractType.CLT,
  grossAmount: Amount = Amount.BRL(8935031),
  companyCnpj: Option[String] = Some("83113309001235"),
  startDate: DateTime = DateTime.now.minusYears(7),
  endDate: Option[DateTime] = Some(DateTime.now),
) {
  def build: FinancialContractUpdateInput = FinancialContractUpdateInput(
    name = name,
    contractType = contractType.toString,
    grossAmount = grossAmount,
    companyCnpj = companyCnpj,
    startDate = startDate,
    endDate = endDate,
  )
}
