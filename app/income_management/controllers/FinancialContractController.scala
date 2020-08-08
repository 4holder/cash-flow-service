package income_management.controllers

import authorization.AuthorizationHelper
import authorization.exceptions.{AuthorizationException, PermissionDeniedException}
import domain.Amount.AmountPayload
import domain.Occurrences.OccurrencesPayload
import domain.User.UserPayload
import domain._
import income_management.DetailFinancialContractService.DetailedFinancialContract
import income_management.FinancialMovementsProjectionService.FinancialMovementsProjection
import income_management.ResumeFinancialContractsService.FinancialContractResume
import income_management.controllers.FinancialContractController.{FinancialContractRegisterInput, FinancialContractUpdateInput, adaptToProjectionResponse, adaptToResponse}
import income_management.repositories.FinancialContractRepository
import income_management.{DetailFinancialContractService, FinancialMovementsProjectionService, RegisterFinancialContractService, ResumeFinancialContractsService}
import infrastructure.ErrorResponse
import infrastructure.reads_and_writes.JodaDateTime
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.Logging
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class FinancialContractController @Inject()(
                                             cc: ControllerComponents,
                                             registerService: RegisterFinancialContractService,
                                             listService: ResumeFinancialContractsService,
                                             projectionService: FinancialMovementsProjectionService,
                                             detailService: DetailFinancialContractService,
                                             auth: AuthorizationHelper,
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

  def details(id: String): Action[AnyContent] = Action.async { implicit request =>
    auth.authorizeObject(id).flatMap { _ =>
      detailService
        .details(id)
        .map(_.map(adaptToResponse))
        .map(details => Ok(toJson(details)))
    } recover treatFailure
  }

  def registerNewFinancialContract(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.isLoggedIn flatMap { implicit user: User => {
      request.body.validate[FinancialContractRegisterInput].asEither match {
        case Right(input) =>
          registerService
            .register(input)
            .map(adaptToResponse)
            .map(financialContract => Created(toJson(financialContract)))
        case Left(validationErrors) =>
          badFinancialInputPayload(validationErrors)
      }
    }} recover treatFailure
  }

  def updateFinancialContract(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    auth.authorizeObject(id).flatMap { _ => {
      request.body.validate[FinancialContractUpdateInput].asEither match {
        case Right(input) =>
          repository
            .update(id, input)
            .flatMap(_ => repository.getById(id))
            .map(maybeFc => maybeFc.map(adaptToResponse))
            .map(financialContract => Ok(toJson(financialContract)))
        case Left(validationErrors) => badFinancialInputPayload(validationErrors)
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

  private def badFinancialInputPayload(validationErros: Seq[(JsPath, Seq[JsonValidationError])]): Future[Result] = {
    Future.successful(BadRequest(Json.toJson(
      ErrorResponse("Invalid financial contract input.", validationErros)
    )))
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
  implicit val financialContractResponse: OWrites[IsolatedFinancialContractResponse] =
    Json.writes[IsolatedFinancialContractResponse]
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
  implicit val incomeRegisterDiscountResponse: Writes[IncomeDiscountResponse] =
    Json.writes[IncomeDiscountResponse]
  implicit val incomeRegisterResponse: Writes[IncomeResponse] =
    Json.writes[IncomeResponse]
  implicit val financialContractRegisterResponse: Writes[FinancialContractResponse] =
    Json.writes[FinancialContractResponse]
  implicit val financialContractUpdateInput: Reads[FinancialContractUpdateInput] =
    Json.reads[FinancialContractUpdateInput]
  implicit val detailedIncomeDiscountResponse: Writes[DetailedIncomeDiscountResponse] =
    Json.writes[DetailedIncomeDiscountResponse]
  implicit val detailedIncomeResponse: Writes[DetailedIncomeResponse] =
    Json.writes[DetailedIncomeResponse]
  implicit val detailedFinancialContractResponse: Writes[DetailedFinancialContractResponse] =
    Json.writes[DetailedFinancialContractResponse]

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
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
    incomes: List[IncomeRegisterInput],
  )

  sealed case class FinancialContractUpdateInput(
    name: String,
    contractType: String,
    grossAmount: AmountPayload,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
  )

  sealed case class IncomeDiscountResponse(
    id: String,
    name: String,
    amount: AmountPayload,
    discountType: String,
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  sealed case class IncomeResponse(
    id: String,
    name: String,
    amount: AmountPayload,
    incomeType: String,
    occurrences: OccurrencesPayload,
    discounts: Seq[IncomeDiscountResponse],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  sealed case class FinancialContractResponse(
    id: String,
    name: String,
    contractType: String,
    companyCnpj: Option[String],
    startDate: DateTime,
    endDate: Option[DateTime],
    incomes: Seq[IncomeResponse],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  sealed case class IsolatedFinancialContractResponse(
    id: String,
    user: UserPayload,
    name: String,
    contractType: String,
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

  case class DetailedIncomeDiscountResponse(
    id: String,
    name: String,
    discountType: String,
    amount: AmountPayload,
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  case class DetailedIncomeResponse(
    id: String,
    name: String,
    grossAmount: AmountPayload,
    netAmount: AmountPayload,
    incomeType: String,
    occurrences: Occurrences,
    discounts: Seq[DetailedIncomeDiscountResponse],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  case class DetailedFinancialContractResponse(
    id: String,
    name: String,
    contractType: String,
    companyCnpj: Option[String],
    incomes: Seq[DetailedIncomeResponse],
    totalYearlyGrossAmount: AmountPayload,
    totalYearlyNetAmount: AmountPayload,
    totalYearlyDiscountAmount: AmountPayload,
    startDate: DateTime,
    endDate: Option[DateTime],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  def adaptToResponse(financialContract: FinancialContract): IsolatedFinancialContractResponse = {
    IsolatedFinancialContractResponse(
      id = financialContract.id,
      user = financialContract.user,
      name = financialContract.name,
      contractType = financialContract.contractType.toString,
      companyCnpj = financialContract.companyCnpj,
      startDate = financialContract.startDate,
      endDate = financialContract.endDate,
      createdAt = financialContract.createdAt,
      modifiedAt = financialContract.modifiedAt
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

  def adaptToResponse(fullContract: (FinancialContract, Seq[(Income, Seq[IncomeDiscount])])): FinancialContractResponse = {
    val contract = fullContract._1

    FinancialContractResponse(
      id = contract.id,
      name = contract.name,
      contractType = contract.contractType.toString,
      companyCnpj = contract.companyCnpj,
      startDate = contract.startDate,
      endDate = contract.endDate,
      incomes = fullContract._2.map {
        case (income, discounts) =>
          IncomeResponse(
            id = income.id,
            name = income.name,
            amount = income.amount,
            incomeType = income.incomeType.toString,
            occurrences = income.occurrences,
            discounts = discounts.map(discount => IncomeDiscountResponse(
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

  def adaptToResponse(detailedFinancialContract: DetailedFinancialContract): DetailedFinancialContractResponse = {
    DetailedFinancialContractResponse(
      id = detailedFinancialContract.id,
      name = detailedFinancialContract.name,
      contractType = detailedFinancialContract.contractType.toString,
      companyCnpj = detailedFinancialContract.companyCnpj,
      totalYearlyDiscountAmount = detailedFinancialContract.totalYearlyDiscountAmount,
      totalYearlyGrossAmount = detailedFinancialContract.totalYearlyGrossAmount,
      totalYearlyNetAmount = detailedFinancialContract.totalYearlyNetAmount,
      incomes = detailedFinancialContract.incomes.map(income => DetailedIncomeResponse(
        id = income.id,
        name = income.name,
        grossAmount = income.grossAmount,
        netAmount = income.netAmount,
        incomeType = income.incomeType.toString,
        discounts = income.discounts.map(discount => DetailedIncomeDiscountResponse(
          id = discount.id,
          name = discount.name,
          discountType = discount.discountType.toString,
          amount = discount.amount,
          createdAt = discount.createdAt,
          modifiedAt = discount.modifiedAt,
        )),
        occurrences = income.occurrences,
        createdAt = income.createdAt,
        modifiedAt = income.modifiedAt,
      )),
      startDate = detailedFinancialContract.startDate,
      endDate = detailedFinancialContract.endDate,
      createdAt = detailedFinancialContract.createdAt,
      modifiedAt = detailedFinancialContract.modifiedAt,
    )
  }
}
