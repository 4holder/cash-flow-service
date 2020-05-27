package utils.builders

import java.util.UUID.randomUUID

import domain.{Amount, Occurrences}
import domain.income.{Income, IncomeType}
import org.joda.time.DateTime

case class IncomeBuilder(
  id: String = randomUUID().toString,
  financialContractId: String = randomUUID().toString,
  name: String = "An Awesome Income",
  amount: Amount = Amount.BRL(132000),
  incomeType: IncomeType.Value = IncomeType.SALARY,
  occurrences: Occurrences = Occurrences.builder.day(5).allMonths.build,
  createdAt: DateTime = DateTime.now,
  modifiedAt: DateTime = DateTime.now,
) {
  def build: Income = Income(
    id = id,
    financialContractId = financialContractId,
    name = name,
    amount = amount,
    incomeType = incomeType,
    occurrences = occurrences,
    createdAt = createdAt,
    modifiedAt = modifiedAt,
  )
}
