package income_management.payloads

import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}
import wire.AmountPayload
import wire.AmountPayload.AmountPayloadImplicits

case class FinancialContractInput(
 name: String,
 contractType: String,
 grossAmount: AmountPayload,
 companyCnpj: Option[String],
 startDate: DateTime,
 endDate: Option[DateTime],
)

object FinancialContractInput extends JodaDateTime with AmountPayloadImplicits {
  implicit val financialContractInput: Reads[FinancialContractInput] = Json.reads[FinancialContractInput]
}
