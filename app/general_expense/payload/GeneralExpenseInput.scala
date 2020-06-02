package general_expense.payload

import domain.Amount.AmountPayload
import general_expense.payload.GeneralExpenseInput.ExpenseType
import play.api.libs.json.{Json, Reads, Writes}
import domain.Occurrences.OccurrencesPayload

case class GeneralExpenseInput(amount: AmountPayload,
                               occurrences: OccurrencesPayload,
                               expenseType: Option[ExpenseType],
                               predictable: Option[Boolean],
                               fixedAmount: Option[Boolean])

object GeneralExpenseInput {

  implicit val generalExpenseReads: Reads[GeneralExpenseInput] = Json.reads[GeneralExpenseInput]
  implicit val generalExpenseWrites: Writes[GeneralExpenseInput] = Json.writes[GeneralExpenseInput]

  type ExpenseType = String

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
}

case class InvalidExpense(listFaults: List[String])
object InvalidExpense {

  implicit val writes: Writes[InvalidExpense] = Json.writes[InvalidExpense]

}