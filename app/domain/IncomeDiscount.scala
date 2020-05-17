package domain

case class IncomeDiscount(
  name: String,
  discountType: IncomeDiscountType.Value,
  amount: Amount,
  grossAmountAliquot: Double
)
