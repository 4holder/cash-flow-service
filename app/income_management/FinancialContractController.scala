package income_management

import domain.User
import income_management.models.financial_contract.FinancialContractRepository
import income_management.payloads.{FinancialContractInput, FinancialContractResponse}
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.libs.json.Json.toJson
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import infrastructure.AuthorizedUser.getUser
import scala.concurrent.{ExecutionContext, Future}

class FinancialContractController @Inject()(cc: ControllerComponents,
                                            repository: FinancialContractRepository,
                                            registerService: RegisterFinancialContractService)
                                           (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def listFinancialContracts(page: Int, pageSize: Int): Action[AnyContent] = Action.async { implicit request =>
    getUser flatMap { implicit user: User =>
      repository
        .getFinancialContracts(page, pageSize)
        .map(_.map(fc => fc: FinancialContractResponse))
        .map(financialContracts => Ok(toJson(financialContracts)))
    }
  }

  def registerNewFinancialContract(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    getUser flatMap { implicit user: User => {
      request.body.validate[FinancialContractInput].asOpt match {
        case Some(input) =>
          registerService
            .register(input)
            .map(fc => fc: FinancialContractResponse)
            .map(financialContract => Ok(toJson(financialContract)))
        case _ =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid financial contract input.")))
      }
    }}
  }

  def deleteFinancialContract(id: String): Action[AnyContent] = Action.async { implicit request =>
    getUser flatMap { implicit user: User =>
      repository
        .deleteFinancialContract(id)
        .map(_ => NoContent)
    }
  }
}
