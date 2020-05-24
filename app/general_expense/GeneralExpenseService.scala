package general_expense

import com.google.inject.Singleton
import domain.{Amount, Currency, Occurrences}
import general_expense.payload.GeneralExpenseInput

import scala.util.{Failure, Success, Try}

@Singleton
class GeneralExpenseService {

  val validExpenseTypes = List(
    "generic",
    "house hold",
    "transportation",
    "food",
    "health",
    "investments",
    "work",
    "shopping",
    "taxes"
  )

  def validExpenseType(expenseType: Option[String]): Option[String] = {
    if(expenseType.isEmpty) None
    else validExpenseTypes.contains(expenseType.get) match {
      case true => None
      case false => Some("invalid expense type")
    }
  }

  def validateInput(input: GeneralExpenseInput): List[String] = {
    val listOfFaults = List(validExpenseType(input.expenseType)).filter(_.isDefined).map(_.get)
    listOfFaults
  }

  def generateExpense(input: GeneralExpenseInput): Try[GeneralExpense] = {

    val amount: Amount = Amount(input.amount.valueInCents, Currency.BRL)
    val occurrences: Occurrences = Occurrences(input.occurrences.day, input.occurrences.months)
    val fixAmount = if(input.fixedAmount.isDefined) input.fixedAmount.get else true
    val expenseType = if(input.expenseType.isDefined) input.expenseType.get else "generic"

    val listFaults = validateInput(input)
    if (listFaults.isEmpty)
      Success(GeneralExpense(amount, occurrences, fixAmount ,expenseType))
    else
      Failure(InvalidExpense(input, listFaults))
  }

}

case class InvalidExpense(input: GeneralExpenseInput, listFaults: List[String]) extends Exception
