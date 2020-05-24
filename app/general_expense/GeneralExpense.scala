package general_expense

import domain.{Amount, Occurrences}
import play.api.libs.json.{Json, Writes}

case class GeneralExpense(amount: Amount,
                          occurrences: Occurrences,
                          fixedAmount: Boolean = true,
                          category: String = "generic")

object GeneralExpense {

  implicit val wireWritable: Writes[GeneralExpense] = Json.writes[GeneralExpense]

}