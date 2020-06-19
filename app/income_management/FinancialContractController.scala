package income_management

import authorization.AuthorizationHelper
import authorization.exceptions.{AuthorizationException, PermissionDeniedException}
import domain.Amount.AmountPayload
import domain.Occurrences.OccurrencesPayload
import domain.User.UserPayload
import domain.{Amount, FinancialContract, Income, IncomeDiscount, User}
import income_management.FinancialContractController.{
  FinancialContractRegisterInput,
  FinancialContractResponse,
  adaptToResponse,
  adaptToProjectionResponse,
}
import income_management.FinancialMovementsProjectionService.FinancialMovementsProjection
import income_management.ResumeFinancialContractsService.FinancialContractResume
import income_management.repositories.FinancialContractRepository
import infrastructure.ErrorResponse
import infrastructure.reads_and_writes.JodaDateTime
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logging
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsValue, Json, OWrites, Reads, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class FinancialContractController @Inject()(
  cc: ControllerComponents,
  registerService: RegisterFinancialContractService,
  listService: ResumeFinancialContractsService,
  projectionService: FinancialMovementsProjectionService,
  auth: AuthorizationHelper
)(implicit ec: ExecutionContext, repository: FinancialContractRepository)
  extends AbstractController(cc) with JodaDateTime with Logging {

  def listIncomeResumes(page: Int, pageSize: Int): Action[AnyContent] =
    Action.async { implicit request =>
      auth.isLoggedIn.flatMap { user: User =>
      listService
        .list(user, page, pageSize)
        .map(adaptToResponse)
        .map(financialContracts => Ok(toJson(financialContracts)))
      } recover treatFailure
    }

  def yearlyIncomeProjections(page: Int, pageSize: Int): Action[AnyContent] = {
    Action.async { implicit request =>
      auth.isLoggedIn.flatMap { user: User =>
        projectionService
          .project(user, page, pageSize)
          .map(adaptToProjectionResponse)
          .map(projections => Ok(toJson(projections)))
      } recover treatFailure
    }
  }

  def getIncomeDetails(id: String): Action[AnyContent] = Action.async { implicit request =>
    auth.authorizeObject(id).flatMap { _ =>
      repository
        .getById(id)
        .map(_.map(fc => fc: FinancialContractResponse))
        .map(financialContract => Ok(toJson(financialContract)))
    } recover treatFailure
  }

  def registerNewFinancialContract(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.isLoggedIn flatMap { implicit user: User => {
      request.body.validate[FinancialContractRegisterInput].asOpt match {
        case Some(input) =>
          registerService
            .register(input)
            .map(adaptToResponse)
            .map(financialContract => Ok(toJson(financialContract)))
        case _ => badFinancialInputPayload
      }
    }} recover treatFailure
  }

  def updateFinancialContract(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.authorizeObject(id).flatMap { _ => {
      request.body.validate[FinancialContractRegisterInput].asOpt match {
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
        _ <- auth.authorizeObject(id)
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

object FinancialContractController extends JodaDateTime {
  implicit val financialContractResumeResponse: OWrites[FinancialContractResumeResponse] =
    Json.writes[FinancialContractResumeResponse]
  implicit val financialContractResponse: OWrites[FinancialContractResponse] =
    Json.writes[FinancialContractResponse]
  implicit val projectionPointResponse: OWrites[ProjectionPointResponse] =
    Json.writes[ProjectionPointResponse]
  implicit val financialMovementsProjectionsProjection: OWrites[FinancialMovementsProjectionResponse] =
    Json.writes[FinancialMovementsProjectionResponse]
  implicit val incomeRegisterDiscountInput: Reads[IncomeRegisterDiscountInput] =
    Json.reads[IncomeRegisterDiscountInput]
  implicit val incomeRegisterInput: Reads[IncomeRegisterInput] =
    Json.reads[IncomeRegisterInput]
  implicit val financialContractRegisterInput: Reads[FinancialContractRegisterInput] =
    Json.reads[FinancialContractRegisterInput]
  implicit val incomeRegisterDiscountResponse: Writes[IncomeRegisterDiscountResponse] =
    Json.writes[IncomeRegisterDiscountResponse]
  implicit val incomeRegisterResponse: Writes[IncomeRegisterResponse] =
    Json.writes[IncomeRegisterResponse]
  implicit val financialContractRegisterResponse: Writes[FinancialContractRegisterResponse] =
    Json.writes[FinancialContractRegisterResponse]

  sealed case class IncomeRegisterDiscountInput(
    name: String,
    amount: AmountPayload,
    discountType: String,
  )

  sealed case class IncomeRegisterInput(
    name: String,
    amount: AmountPayload,
    incomeType: String,
    occurrences: OccurrencesPayload,
    discounts: List[IncomeRegisterDiscountInput],
  )

  sealed case class FinancialContractRegisterInput(
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
    incomes: List[IncomeRegisterInput],
  )

  sealed case class IncomeRegisterDiscountResponse(
    id: String,
    name: String,
    amount: AmountPayload,
    discountType: String,
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  sealed case class IncomeRegisterResponse(
    id: String,
    name: String,
    amount: AmountPayload,
    incomeType: String,
    occurrences: OccurrencesPayload,
    discounts: Seq[IncomeRegisterDiscountResponse],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  sealed case class FinancialContractRegisterResponse(
    id: String,
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
    incomes: Seq[IncomeRegisterResponse],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  sealed case class FinancialContractResponse(
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

  sealed case class FinancialContractResumeResponse(
   id: String,
   name: String,
   yearlyGrossIncome: Option[AmountPayload],
   yearlyNetIncome: Option[AmountPayload],
   yearlyIncomeDiscount: Option[AmountPayload]
  )

  sealed case class ProjectionPointResponse(
    amount: Amount,
    dateTime: DateTime
  )

  sealed case class FinancialMovementsProjectionResponse(
    label: String,
    currency: String,
    financialMovements: Seq[ProjectionPointResponse],
  )

  implicit def adaptToResponse(financialContract: FinancialContract): FinancialContractResponse = {
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

  def adaptToResponse(resumes: Seq[FinancialContractResume]): Seq[FinancialContractResumeResponse] = {
    resumes.map(resume =>
      FinancialContractResumeResponse(
        id = resume.id,
        name = resume.name,
        yearlyGrossIncome = resume.yearlyGrossIncome.map(g => g :AmountPayload),
        yearlyNetIncome = resume.yearlyNetIncome.map(i => i: AmountPayload),
        yearlyIncomeDiscount = resume.yearlyIncomeDiscount.map(d => d: AmountPayload)
      )
    )
  }

  def adaptToProjectionResponse(projections: Seq[FinancialMovementsProjection]): Seq[FinancialMovementsProjectionResponse] = {
    projections.map { projection =>
      FinancialMovementsProjectionResponse(
        projection.label,
        currency = projection.currency.toString,
        financialMovements = projection.financialMovements.map(fm =>
          ProjectionPointResponse(
            fm.amount,
            dateTime = fm.dateTime
          )
        )
      )
    }
  }

  def adaptToResponse(fullContract: (FinancialContract, Seq[(Income, Seq[IncomeDiscount])])): FinancialContractRegisterResponse = {
    val contract = fullContract._1

    FinancialContractRegisterResponse(
      id = contract.id,
      name = contract.name,
      contractType = contract.contractType.toString,
      grossAmount = contract.grossAmount,
      companyCnpj = contract.companyCnpj,
      startDate = contract.startDate,
      endDate = contract.endDate,
      incomes = fullContract._2.map {
        case (income, discounts) =>
          IncomeRegisterResponse(
            id = income.id,
            name = income.name,
            amount = income.amount,
            incomeType = income.incomeType.toString,
            occurrences = income.occurrences,
            discounts = discounts.map(discount => IncomeRegisterDiscountResponse(
              id = discount.id,
              name = discount.name,
              amount = discount.amount,
              discountType = discount.discountType.toString,
              createdAt = discount.createdAt,
              modifiedAt = discount.modifiedAt,
            )),
            createdAt = income.createdAt,
            modifiedAt = income.modifiedAt,
          )
      },
      createdAt = contract.createdAt,
      modifiedAt = contract.modifiedAt,
    )
  }
}
