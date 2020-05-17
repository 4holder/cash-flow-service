package domain

case class Income(
  name: String,
  amount: Amount,
  incomeType: IncomeType.Value,
  occurrences: Occurrences,
  discounts: List[IncomeDiscount]
)
