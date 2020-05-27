package clt_contract

import clt_contract.payloads.{CLTContractResponse, CalculateCLTContractInput}
import javax.inject._
import play.api.mvc._
import clt_contract.payloads.CLTContractResponse._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.json.Json._

import scala.concurrent.Future

@Singleton
class CLTCalculatorController @Inject()(
  cc: ControllerComponents,
  cltCalculator: CLTContractCalculatorService)
  extends AbstractController(cc) {

  def calculateCLTContract(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[CalculateCLTContractInput].asOpt match {
      case Some(input) =>
        val cltContract: CLTContractResponse = cltCalculator
          .calculateByGrossSalary(
            input.grossSalary,
            input.dependentQuantities,
            input.deductionsAmount
          )
          .get

        Future.successful(Ok(toJson(cltContract)))
      case _ => Future.successful(BadRequest(Json.obj(
        "error" -> "Missing or invalid input"
      )))
    }
  }
}
