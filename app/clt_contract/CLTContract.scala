package clt_contract

import clt_contract.payloads.IncomeResponse
import domain.Amount
import domain.FinancialContract.ContractType

case class CLTContract(grossSalary: Amount, incomes: List[IncomeResponse]) {
  val contractType: ContractType.Value = ContractType.CLT
}
