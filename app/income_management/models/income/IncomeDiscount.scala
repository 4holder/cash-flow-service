package income_management.models.income

import domain.{Amount, IncomeDiscountType}

case class IncomeDiscount(
  id: String,
  name: String,
  discountType: IncomeDiscountType.Value,
  amount: Amount,
  grossAmountAliquot: Double
)
