package clt_contract

import domain.income.Income.IncomePayload
import domain.{Amount, ContractType}

case class CLTContract(grossSalary: Amount, incomes: List[IncomePayload]) {
  val contractType: ContractType.Value = ContractType.CLT
}
