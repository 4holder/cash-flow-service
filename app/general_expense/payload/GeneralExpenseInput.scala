package general_expense.payload

import clt_contract.payloads.OccurrencesPayload
import general_expense.payload.GeneralExpenseInput.ExpenseType
import play.api.libs.json.{Json, Reads}
import wire.AmountPayload

case class GeneralExpenseInput(amount: AmountPayload,
                               occurrences: OccurrencesPayload,
                               expenseType: Option[ExpenseType],
                               fixedAmount: Option[Boolean])

object GeneralExpenseInput {

  implicit val generalExpenseReads: Reads[GeneralExpenseInput] = Json.reads[GeneralExpenseInput]

  type ExpenseType = String

}
