package general_expense

import com.google.inject.{Inject, Singleton}
import general_expense.payload.{GeneralExpenseInput, GeneralExpenseResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import scala.concurrent.Future
import scala.util.{Failure, Success}

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
            val responseType: GeneralExpenseResponse = expense
            Future.successful(Ok(Json.toJson(responseType)))
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
