package income_management

import authorization.AuthorizationHelper
import authorization.exceptions.{AuthorizationException, PermissionDeniedException}
import domain.Amount.AmountPayload
import domain.IncomeDiscount
import income_management.repositories.IncomeDiscountRepository
import infrastructure.ErrorResponse
import infrastructure.reads_and_writes.JodaDateTime
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logging
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

class IncomeDiscountController @Inject()(
  cc: ControllerComponents,
  auth: AuthorizationHelper
)(implicit ec: ExecutionContext, repository: IncomeDiscountRepository) extends AbstractController(cc) with Logging {
//  def registerNewIncomeDiscount()

  private def treatFailure: PartialFunction[Throwable, Result] = {
    case _: PermissionDeniedException => NotFound(Json.toJson(ErrorResponse.notFound))
    case e: AuthorizationException => Unauthorized(Json.toJson(ErrorResponse(e)))
    case e =>
      logger.error(e.getMessage, e)
      InternalServerError(Json.toJson(ErrorResponse(e)))
  }
}

object IncomeDiscountController {
  case class IncomeDiscountResponse(
    id: String,
    incomeId: String,
    name: String,
    amount: AmountPayload,
    discountType: String,
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  object IncomeDiscountResponse extends JodaDateTime {
    implicit val incomeDiscountResponse: Writes[IncomeDiscountResponse] = Json.writes[IncomeDiscountResponse]

    implicit def fromIncomeDiscount(incomeDiscount: IncomeDiscount): IncomeDiscountResponse = IncomeDiscountResponse(
      id = incomeDiscount.id,
      incomeId = incomeDiscount.incomeId,
      name = incomeDiscount.name,
      amount = incomeDiscount.amount,
      discountType = incomeDiscount.discountType.toString,
      createdAt = incomeDiscount.createdAt,
      modifiedAt = incomeDiscount.modifiedAt,
    )
  }
}

