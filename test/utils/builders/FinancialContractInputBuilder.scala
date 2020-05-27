package utils.builders

import domain.financial_contract.FinancialContract.FinancialContractPayload
import domain.Amount
import domain.financial_contract.ContractType
import org.joda.time.DateTime

case class FinancialContractInputBuilder(
  name: String = "A Good Contract",
  contractType: ContractType.Value = ContractType.CLT,
  grossAmount: Amount = Amount.BRL(1235000),
  companyCnpj: Option[String] = Some("3311330900014"),
  startDate: DateTime = DateTime.now,
  endDate: Option[DateTime] = Some(DateTime.now),
) {
  def build: FinancialContractPayload = {
    FinancialContractPayload(
      name = name,
      contractType = contractType.toString,
      grossAmount = grossAmount,
      companyCnpj = companyCnpj,
      startDate = startDate,
      endDate = endDate,
    )
  }
}
