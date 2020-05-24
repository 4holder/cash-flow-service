package general_expense.payload

import domain.Amount.AmountPayload
import domain.Occurrences.OccurrencesPayload
import general_expense.GeneralExpense
import general_expense.payload.GeneralExpenseInput.ExpenseType
import play.api.libs.json.{Json, Writes}


case class GeneralExpenseResponse(amount: AmountPayload,
                             occurrences: OccurrencesPayload,
                             expenseType: Option[ExpenseType],
                             fixedAmount: Option[Boolean])

object GeneralExpenseResponse {
  implicit val fromExpenseToResponse: GeneralExpense => GeneralExpenseResponse = { expense => {
      GeneralExpenseResponse(
        expense.amount,
        expense.occurrences,
        Some(expense.category),
        Some(expense.fixedAmount)
      )
    }
  }
  implicit val jsonWriter : Writes[GeneralExpenseResponse] = Json.writes[GeneralExpenseResponse]
}
