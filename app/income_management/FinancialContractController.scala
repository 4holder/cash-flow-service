package income_management

import authorization.AuthorizationHelper
import authorization.exceptions.{AuthorizationException, PermissionDeniedException}
import domain.Amount.AmountPayload
import domain.FinancialContract.FinancialContractPayload
import domain.User.UserPayload
import domain.{FinancialContract, User}
import income_management.FinancialContractController.FinancialContractResponse
import infrastructure.ErrorResponse
import infrastructure.reads_and_writes.JodaDateTime
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logging
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

class FinancialContractController @Inject()(
  cc: ControllerComponents,
  repository: FinancialContractRepository,
  registerService: RegisterFinancialContractService,
  auth: AuthorizationHelper
)(implicit ec: ExecutionContext) extends AbstractController(cc) with Logging {
  def listFinancialContracts(page: Int, pageSize: Int): Action[AnyContent] = Action.async { implicit request =>
    auth.isLoggedIn.flatMap { user: User =>
      repository
        .allByUser(user.id, page, pageSize)
        .map(_.map(fc => fc: FinancialContractResponse))
        .map(financialContracts => Ok(toJson(financialContracts)))
    } recover treatFailure
  }

  def getFinancialContractById(id: String): Action[AnyContent] = Action.async { implicit request =>
    auth.authorizeByFinancialContract(id).flatMap { _ =>
      repository
        .getById(id)
        .map(_.map(fc => fc: FinancialContractResponse))
        .map(financialContract => Ok(toJson(financialContract)))
    } recover treatFailure
  }

  def registerNewFinancialContract(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.isLoggedIn flatMap { implicit user: User => {
      request.body.validate[FinancialContractPayload].asOpt match {
        case Some(input) =>
          registerService
            .register(input)
            .map(fc => fc: FinancialContractResponse)
            .map(financialContract => Ok(toJson(financialContract)))
        case _ => badFinancialInputPayload
      }
    }} recover treatFailure
  }

  def updateFinancialContract(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.authorizeByFinancialContract(id).flatMap { _ => {
      request.body.validate[FinancialContractPayload].asOpt match {
        case Some(input) =>
          repository
            .update(id, input)
            .flatMap(_ => repository.getById(id))
            .map(maybeFc => maybeFc.map(fc => fc: FinancialContractResponse))
            .map(financialContract => Ok(toJson(financialContract)))
        case _ => badFinancialInputPayload
        }
    }} recover treatFailure
  }

  def deleteFinancialContract(id: String): Action[AnyContent] = Action.async { implicit request =>
    (
      for {
        _ <- auth.authorizeByFinancialContract(id)
        _ <- repository.delete(id)
      } yield NoContent
    ) recover treatFailure
  }

  private def badFinancialInputPayload: Future[Result] = {
    Future.successful(BadRequest(Json.toJson(ErrorResponse("Invalid financial contract input."))))
  }

  private def treatFailure: PartialFunction[Throwable, Result] = {
    case _: PermissionDeniedException => NotFound(Json.toJson(ErrorResponse.notFound))
    case e: AuthorizationException => Unauthorized(Json.toJson(ErrorResponse(e)))
    case e =>
      logger.error(e.getMessage, e)
      InternalServerError(Json.toJson(ErrorResponse(e)))
  }
}

object FinancialContractController {
  case class FinancialContractResponse(
    id: String,
    user: UserPayload,
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
    createdAt: DateTime,
    modifiedAt: DateTime
  )

  object FinancialContractResponse extends JodaDateTime
    with UserPayload.ReadsAndWrites {
    implicit val financialContractResponse: Writes[FinancialContractResponse] = Json.writes[FinancialContractResponse]

    implicit def fromFinancialContract(financialContract: FinancialContract): FinancialContractResponse = {
      FinancialContractResponse(
        id = financialContract.id,
        user = financialContract.user,
        name = financialContract.name,
        contractType = financialContract.contractType.toString,
        grossAmount = financialContract.grossAmount,
        companyCnpj = financialContract.companyCnpj,
        startDate = financialContract.startDate,
        endDate = financialContract.endDate,
        createdAt = financialContract.createdAt,
        modifiedAt = financialContract.modifiedAt
      )
    }
  }
}
