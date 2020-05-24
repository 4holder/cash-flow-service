package general_expense.payload

import play.api.libs.json.{Json, Writes}

case class GeneralExpenseResponse()

object GeneralExpenseResponse {
  implicit val jsonWriter: Writes[GeneralExpenseResponse] = Json.writes[GeneralExpenseResponse]
}
