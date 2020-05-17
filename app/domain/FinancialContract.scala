package domain

trait FinancialContract {
  val contractType: ContractType.Value
  val incomes: List[Income]
}
