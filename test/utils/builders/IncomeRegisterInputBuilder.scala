package utils.builders

import domain.Amount.AmountPayload
import domain.Income.IncomeType
import domain.IncomeDiscount.IncomeDiscountType
import domain.Occurrences.OccurrencesPayload
import domain.{Amount, Occurrences}
import income_management.controllers.FinancialContractController.{IncomeRegisterDiscountInput, IncomeRegisterInput}

case class IncomeRegisterInputBuilder(
  name: String = "An Income",
  amount: AmountPayload = Amount.BRL(8935031),
  incomeType: IncomeType.Value = IncomeType.SALARY,
  occurrences: OccurrencesPayload = Occurrences.builder.allMonths.day(4).build,
  discounts: List[IncomeRegisterDiscountInput] = List(
    IncomeRegisterDiscountInputBuilder(
      amount = Amount.BRL(13501),
      discountType = IncomeDiscountType.INSS,
    ).build,
    IncomeRegisterDiscountInputBuilder(
      amount = Amount.BRL(25019),
      discountType = IncomeDiscountType.IRRF,
    ).build
  ),
) {
  def build: IncomeRegisterInput = IncomeRegisterInput(
    name = name,
    amount = amount,
    incomeType = incomeType.toString,
    occurrences = occurrences,
    discounts = discounts,
  )
}
