package income_management.payloads

import domain.Amount.AmountPayload
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

case class FinancialContractInput(
 name: String,
 contractType: String,
 grossAmount: AmountPayload,
 companyCnpj: Option[String],
 startDate: DateTime,
 endDate: Option[DateTime],
)

object FinancialContractInput extends JodaDateTime with AmountPayload.ReadsAndWrites {
  implicit val financialContractInput: Reads[FinancialContractInput] = Json.reads[FinancialContractInput]
}
