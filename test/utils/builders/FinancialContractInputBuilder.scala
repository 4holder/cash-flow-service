package utils.builders

import domain.{Amount, ContractType}
import income_management.payloads.FinancialContractInput
import org.joda.time.DateTime

case class FinancialContractInputBuilder(
  name: String = "A Good Contract",
  contractType: ContractType.Value = ContractType.CLT,
  grossAmount: Amount = Amount.BRL(1235000),
  companyCnpj: Option[String] = Some("3311330900014"),
  startDate: DateTime = DateTime.now,
  endDate: Option[DateTime] = Some(DateTime.now),
) {
  def build: FinancialContractInput = {
    FinancialContractInput(
      name = name,
      contractType = contractType.toString,
      grossAmount = grossAmount,
      companyCnpj = companyCnpj,
      startDate = startDate,
      endDate = endDate,
    )
  }
}
