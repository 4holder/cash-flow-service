package utils.builders

import domain.Amount
import domain.FinancialContract.{ContractType, FinancialContractPayload}
import org.joda.time.DateTime

case class FinancialContractPayloadBuilder(
  name: String = "A Good Contract Payload",
  contractType: ContractType.Value = ContractType.CLT,
  grossAmount: Amount = Amount.BRL(8935031),
  companyCnpj: Option[String] = Some("93113309001234"),
  startDate: DateTime = DateTime.now.minusYears(7),
  endDate: Option[DateTime] = Some(DateTime.now),
) {
  def build: FinancialContractPayload = {
    FinancialContractPayload(
      name,
      contractType.toString,
      grossAmount,
      companyCnpj,
      startDate,
      endDate,
    )
  }
}