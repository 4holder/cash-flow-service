package clt_contract

import clt_contract.payloads.CalculateCLTContractInput
import javax.inject._
import play.api.libs.json.Json._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import clt_contract.payloads.CLTContractResponse.adaptToResponse
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CLTCalculatorController @Inject()(
  cc: ControllerComponents,
  cltCalculator: CLTContractCalculatorService
)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def calculateCLTContract(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CalculateCLTContractInput].asOpt match {
      case Some(input) =>
        Future
          .fromTry(cltCalculator.calculateByGrossSalary(
            input.grossSalary,
            input.dependentQuantities,
            input.deductionsAmount
          ))
          .map(adaptToResponse)
          .map(cltContract => Ok(toJson(cltContract)))
      case _ => Future.successful(BadRequest(Json.obj(
        "error" -> "Missing or invalid input"
      )))
    }
  }
}
