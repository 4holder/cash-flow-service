package general_expense


import domain.Currency
import general_expense.payload.{GeneralExpenseInput, InvalidExpense}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import domain.Amount._
import domain.Occurrences._

import scala.util.Failure

class GeneralExpenseServiceTest extends WordSpecLike with BeforeAndAfterAll {
  val generalExpenseService = new GeneralExpenseService

  "the General Expense Service " should {

    "generate a General Expense with minimum information and default optional values" in {

      val input: GeneralExpenseInput = GeneralExpenseInput(
        AmountPayload(100L, "BRL"),
        OccurrencesPayload(5, List(1,2,3)),None, None)
      val expense: GeneralExpense = generalExpenseService.generateExpense(input).left.get

      assert(expense.amount.valueInCents == 100L, "the value in cents was to be 100")
      assert(expense.amount.currency == Currency.BRL, "the currency is BRL")
      assert(expense.occurrences.day == 5, "the day of the month for this expense was to be 5")
      assert(expense.occurrences.months == List(1,2,3), "the list of months expected was 1,2,3")
      assert(expense.fixedAmount)
    }

    "generate a General Expense with optional values as well" in {

      val input: GeneralExpenseInput = GeneralExpenseInput(
        AmountPayload(550L, "BRL"),
        OccurrencesPayload(10, List(1,2,3,4,5,6,7,8,9,10,11,12)),
        expenseType = Some("house hold"),
        fixedAmount = Some(true))

      val expense = generalExpenseService.generateExpense(input).left.get

      assert(expense.amount.valueInCents == 550L, "the value in cents was to be 550")
      assert(expense.amount.currency == Currency.BRL, "the currency is BRL")
      assert(expense.occurrences.day == 10, "the day of the month for this expense was to be 10")
      assert(expense.occurrences.months == List(1,2,3,4,5,6,7,8,9,10,11,12), "the list of months expected was all months")
      assert(expense.fixedAmount)
      assert(expense.category == "house hold")
    }

    "ignore or fail if type of expense is not an unrecognized value" in {
      val input: GeneralExpenseInput = GeneralExpenseInput(
        AmountPayload(550L, "BRL"),
        OccurrencesPayload(10, List(1,2,3,4,5,6,7,8,9,10,11,12)),
        expenseType = Some("crazy type"),
        fixedAmount = None)

      val expense = generalExpenseService.generateExpense(input).right.get

      assert(expense.listFaults.size == 1)
    }
  }

}
