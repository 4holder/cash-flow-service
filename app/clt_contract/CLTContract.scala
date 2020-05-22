package clt_contract

import domain.{Amount, ContractType, Income}

case class CLTContract(grossSalary: Amount, incomes: List[Income]) {
  val contractType: ContractType.Value = ContractType.CLT
}
