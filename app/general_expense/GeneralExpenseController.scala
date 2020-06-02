package general_expense

import com.google.inject.{Inject, Singleton}
import general_expense.payload.{GeneralExpenseInput, GeneralExpenseResponse}
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import payload.GeneralExpenseInput._

import scala.concurrent.Future


@Singleton
class GeneralExpenseController @Inject()
(  cc: ControllerComponents,
   geService: GeneralExpenseService
) extends AbstractController(cc) {



  def calculateExpense: Action[JsValue] = Action.async(parse.json) { request =>

    request.body.validate[GeneralExpenseInput].asEither match {
      case Right(inp: GeneralExpenseInput) => {

        val expenseContract = geService.generateExpense(inp) match {

          case Left(expense: GeneralExpense) => {
            val responseType: GeneralExpenseResponse = expense
            Future.successful(Ok(Json.toJson(responseType)))
          }

          case Right(fail) => {
            Future.successful(BadRequest(Json.toJson(fail)))
          }
        }

        expenseContract
      }

      case Left(invalid) => {
        val pretty = "invalid: " + invalid.toList.map(_._1.toJsonString)
          .mkString("; invalid: ")
        Future.successful(BadRequest(
          Json.obj("error"-> pretty)
        ))
      }
    }
  }

}
