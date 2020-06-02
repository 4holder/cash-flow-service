package general_expense

import com.google.inject.Singleton
import domain.{Amount, Currency, Occurrences}
import general_expense.payload.{GeneralExpenseInput, InvalidExpense}

@Singleton
class GeneralExpenseService {

  def validExpenseType(expenseType: Option[String]): Option[String] = {
    if(expenseType.isEmpty) None
    else GeneralExpenseInput.validExpenseTypes.contains(expenseType.get) match {
      case true => None
      case false => Some(s"invalid expense type: here are the valid options ${GeneralExpenseInput.validExpenseTypes}")
    }
  }

  def validateInput(input: GeneralExpenseInput): List[String] = {
    val listOfFaults = List(validExpenseType(input.expenseType)).filter(_.isDefined).map(_.get)
    listOfFaults
  }

  def generateExpense(input: GeneralExpenseInput): Either[GeneralExpense, InvalidExpense] = {

    val amount: Amount = Amount(input.amount.valueInCents, Currency.BRL)
    val occurrences: Occurrences = Occurrences(input.occurrences.day, input.occurrences.months)
    val fixAmount = if(input.fixedAmount.isDefined) input.fixedAmount.get else true
    val expenseType = if(input.expenseType.isDefined) input.expenseType.get else "generic"
    val predictable = if(input.predictable.isDefined) input.predictable.get else false

    val listFaults = validateInput(input)
    if (listFaults.isEmpty)
      Left(GeneralExpense(amount, occurrences, fixAmount , predictable, expenseType))
    else
      Right(InvalidExpense(listFaults))
  }

}

