package general_expense.payload

import clt_contract.payloads.OccurrencesPayload
import general_expense.payload.GeneralExpenseInput.ExpenseType
import play.api.libs.json.{Json, Reads}
import wire.AmountPayload
import wire.AmountPayload.AmountPayloadImplicits

case class GeneralExpenseInput(amount: AmountPayload,
                               occurrences: OccurrencesPayload,
                               expenseType: Option[ExpenseType],
                               fixedAmount: Option[Boolean])

object GeneralExpenseInput extends AmountPayloadImplicits {

  implicit val generalExpenseReads: Reads[GeneralExpenseInput] = Json.reads[GeneralExpenseInput]

  type ExpenseType = String

}
