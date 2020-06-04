package utils.builders

import java.util.UUID.randomUUID

import domain.Income.IncomeType
import domain.IncomeDiscount.IncomeDiscountType
import domain.{Amount, Income, IncomeDiscount}
import org.joda.time.DateTime

case class IncomeDiscountBuilder(
  id: String = randomUUID().toString,
  incomeId: String = randomUUID().toString,
  name: String = "An Awesome Income",
  amount: Amount = Amount.BRL(132000),
  discountType: IncomeDiscountType.Value = IncomeDiscountType.INSS,
  grossAmountAliquot: Double =  19283.01,
  createdAt: DateTime = DateTime.now,
  modifiedAt: DateTime = DateTime.now,
) {
  def build: IncomeDiscount = IncomeDiscount(
    id = id,
    incomeId = incomeId,
    name = name,
    amount = amount,
    discountType = discountType,
    aliquot = grossAmountAliquot,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
  )
}
