package general_expense.payload

import domain.Amount.AmountPayload
import general_expense.payload.GeneralExpenseInput.ExpenseType
import play.api.libs.json.{Json, Reads}
import domain.Occurrences.OccurrencesPayload

case class GeneralExpenseInput(amount: AmountPayload,
                               occurrences: OccurrencesPayload,
                               expenseType: Option[ExpenseType],
                               fixedAmount: Option[Boolean])

object GeneralExpenseInput {

  implicit val generalExpenseReads: Reads[GeneralExpenseInput] = Json.reads[GeneralExpenseInput]

  type ExpenseType = String

}
