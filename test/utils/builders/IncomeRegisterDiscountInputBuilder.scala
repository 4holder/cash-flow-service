package utils.builders

import domain.Amount
import domain.Amount.AmountPayload
import domain.IncomeDiscount.IncomeDiscountType
import income_management.FinancialContractController.IncomeRegisterDiscountInput

case class IncomeRegisterDiscountInputBuilder(
  name: String = "An Income",
  amount: AmountPayload = Amount.BRL(13531),
  discountType: IncomeDiscountType.Value = IncomeDiscountType.INSS,
) {
  def build: IncomeRegisterDiscountInput = IncomeRegisterDiscountInput(
    name = name,
    amount = amount,
    discountType = discountType.toString,
  )
}
