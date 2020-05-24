package income_management.models.income

import domain.{Amount, IncomeType, Occurrences}

case class Income(
  id: String,
  amount: Amount,
  incomeType: IncomeType.Value,
  occurrences: Occurrences,
  discounts: List[IncomeDiscount]
)