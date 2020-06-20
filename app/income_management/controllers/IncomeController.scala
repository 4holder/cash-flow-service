package income_management.controllers

import authorization.AuthorizationHelper
import authorization.exceptions.{AuthorizationException, PermissionDeniedException}
import domain.Amount.AmountPayload
import domain.Income
import domain.Income.IncomePayload
import domain.Occurrences.OccurrencesPayload
import income_management.RegisterIncomeService
import income_management.controllers.IncomeController.IncomeResponse
import income_management.repositories.IncomeRepository
import infrastructure.ErrorResponse
import infrastructure.reads_and_writes.JodaDateTime
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logging
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class IncomeController @Inject()(
  cc: ControllerComponents,
  service: RegisterIncomeService,
  auth: AuthorizationHelper
)(implicit repository: IncomeRepository, ec: ExecutionContext) extends AbstractController(cc) with Logging {
  def updateIncome(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.authorizeObject(id) flatMap { _ => {
      request.body.validate[IncomePayload].asEither match {
        case Right(input) =>
          repository
            .update(id, input)
            .flatMap(_ => repository.getById(id))
            .map(_.map(maybeIncome => maybeIncome: IncomeResponse))
            .map(income => Ok(toJson(income)))
        case Left(e) =>
          badIncomePayload(e)
      }
    }}
  }

  def deleteIncome(id: String): Action[AnyContent] = Action.async { implicit request =>
    (
      for {
        _ <- auth.authorizeObject(id)
        _ <- repository.delete(id)
      } yield NoContent
    ) recover treatFailure
  }

  private def badIncomePayload(e: Seq[(JsPath, Seq[JsonValidationError])]): Future[Result] = {
    val message = e.map(error => (error._1.toString(), error._2.map(_.message).mkString(";")))
    logger.error(s"Invalid contract received. ${message}")
    Future.successful(BadRequest(Json.toJson(ErrorResponse(s"Invalid income input. ${message}"))))
  }

  private def treatFailure: PartialFunction[Throwable, Result] = {
    case _: PermissionDeniedException => NotFound(Json.toJson(ErrorResponse.notFound))
    case e: AuthorizationException => Unauthorized(Json.toJson(ErrorResponse(e)))
    case e =>
      logger.error(e.getMessage, e)
      InternalServerError(Json.toJson(ErrorResponse(e)))
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