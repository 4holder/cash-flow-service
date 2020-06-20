package utils.builders

import java.util.UUID.randomUUID

import domain.FinancialContract.ContractType
import domain.{Amount, FinancialContract, User}
import org.joda.time.DateTime

case class FinancialContractBuilder(
  id: String = randomUUID().toString,
  user: User = User(randomUUID().toString),
  name: String = "A Good Contract",
  contractType: ContractType.Value = ContractType.CLT,
  grossAmount: Amount = Amount.BRL(1235000),
  companyCnpj: Option[String] = Some("3311330900014"),
  startDate: DateTime = DateTime.now,
  endDate: Option[DateTime] = Some(DateTime.now),
  createdAt: DateTime = DateTime.now,
  modifiedAt: DateTime = DateTime.now
) {
  def build: FinancialContract = {
    FinancialContract(
      id,
      user,
      name,
      contractType,
      companyCnpj,
      startDate,
      endDate,
      createdAt,
      modifiedAt
    )
  }
}