package clt_contract

import domain.{Amount, ContractType, FinancialContract, Income}

case class CLTContract(grossSalary: Amount, incomes: List[Income]) extends FinancialContract {
  override val contractType: ContractType.Value = ContractType.CLT
}
