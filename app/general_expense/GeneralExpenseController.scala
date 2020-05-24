package general_expense

import com.google.inject.{Inject, Singleton}
import general_expense.payload.{GeneralExpenseInput}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.json.Json._
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import GeneralExpense._
@Singleton
class GeneralExpenseController @Inject()
(  cc: ControllerComponents,
   geService: GeneralExpenseService
) extends AbstractController(cc) {



  def calculateExpense: Action[JsValue] = Action.async(parse.json) { request =>

    request.body.validate[GeneralExpenseInput].asOpt match {
      case Some(input) => {

        val expenseContract = geService.generateExpense(input) match {
          case Success(expense: GeneralExpense) => {
            Future.successful(Ok(Json.toJson(expense)))
          }
          case Failure(exception) => {
            Future.successful(BadRequest)
          }
        }

        expenseContract
      }
      case None => {
        Future.successful(BadRequest)
      }
    }
  }

}
