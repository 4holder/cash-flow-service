package clt_contract

import domain.Amount
import domain.FinancialContract.ContractType
import domain.Income.IncomePayload

case class CLTContract(grossSalary: Amount, incomes: List[IncomePayload]) {
  val contractType: ContractType.Value = ContractType.CLT
}
