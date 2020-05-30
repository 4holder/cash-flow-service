package income_management

import domain.Amount.AmountPayload
import domain.Income
import domain.Occurrences.OccurrencesPayload
import income_management.IncomeController.IncomeResponse
import infrastructure.AuthorizationService
import infrastructure.reads_and_writes.JodaDateTime
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json.Json.toJson
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class IncomeController @Inject()(cc: ControllerComponents,
                                 repository: IncomeRepository,
                                 auth: AuthorizationService
                                )
                                (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def listIncomes(financialContractId: String,
                  page: Int,
                  pageSize: Int): Action[AnyContent] = Action.async { implicit request =>
    auth.authorizeByFinancialContract(financialContractId)
      .flatMap {
        case true =>
          repository
            .allByFinancialContractId(financialContractId, page, pageSize)
            .map(_.map(fc => fc: IncomeResponse))
            .map(incomes => Ok(toJson(incomes)))
        case _ => Future.successful(NotFound(Json.obj("message" -> "resource not found")))
      }
  }

}

object IncomeController {
  case class IncomeResponse(
    id: String,
    financialContractId: String,
    name: String,
    amount: AmountPayload,
    incomeType: String,
    occurrences: OccurrencesPayload,
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  object IncomeResponse extends JodaDateTime {
    implicit val incomeResponse: Writes[IncomeResponse] = Json.writes[IncomeResponse]

    implicit def fromIncome(income: Income): IncomeResponse = IncomeResponse(
      id = income.id,
      financialContractId = income.financialContractId,
      name = income.name,
      amount = income.amount,
      incomeType = income.incomeType.toString,
      occurrences = income.occurrences,
      createdAt = income.createdAt,
      modifiedAt = income.modifiedAt,
    )
  }
}