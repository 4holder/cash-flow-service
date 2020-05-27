package income_management

import domain.User
import domain.financial_contract.FinancialContract.{FinancialContractPayload, FinancialContractResponse}
import domain.financial_contract.FinancialContractRepository
import infrastructure.AuthorizedUser.getUser
import javax.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

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

  def getFinancialContract(id: String): Action[AnyContent] = Action.async { implicit request =>
    getUser flatMap { implicit user: User =>
      repository
        .getFinancialContractById(id)
        .map(_.map(fc => fc: FinancialContractResponse))
        .map(financialContract => Ok(toJson(financialContract)))
    }
  }

  def registerNewFinancialContract(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    getUser flatMap { implicit user: User => {
      request.body.validate[FinancialContractPayload].asOpt match {
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

  def updateFinancialContract(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    getUser flatMap { implicit user: User => {
      request.body.validate[FinancialContractPayload].asOpt match {
        case Some(input) =>
          repository
            .updateFinancialContract(id, input)
            .flatMap(_ => repository.getFinancialContractById(id))
            .map(maybeFc => maybeFc.map(fc => fc: FinancialContractResponse))
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
