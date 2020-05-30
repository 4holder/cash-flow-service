package utils.builders

import domain.Income.{IncomePayload, IncomeType}
import domain.{Amount, Occurrences}

case class IncomePayloadBuilder(
  name: String = "An Awesome Income",
  amount: Amount = Amount.BRL(132000),
  incomeType: IncomeType.Value = IncomeType.SALARY,
  occurrences: Occurrences = Occurrences.builder.day(5).allMonths.build,
) {
  def build: IncomePayload = IncomePayload(
    name = name,
    amount = amount,
    incomeType = incomeType.toString,
    occurrences = occurrences,
  )
}
