package income_management.models

import domain.{Amount, IncomeDiscountType}

case class IncomeDiscount(
  id: String,
  name: String,
  discountType: IncomeDiscountType.Value,
  amount: Amount,
  grossAmountAliquot: Double
)
