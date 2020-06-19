package utils.builders

import domain.Amount
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}

case class IncomeDiscountPayloadBuilder(
  name: String = "An Awesome Income",
  amount: Amount = Amount.BRL(132000),
  discountType: IncomeDiscountType.Value = IncomeDiscountType.INSS,
  aliquot: Double = 0.11,
) {
  def build: IncomeDiscountPayload = IncomeDiscountPayload(
    name = name,
    amount = amount,
    discountType = discountType.toString,
  )
}
