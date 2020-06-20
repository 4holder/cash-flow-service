package unit.income_management.controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import authorization.AuthorizationHelper
import domain.User
import income_management.controllers.FinancialContractController
import income_management.controllers.FinancialContractController.FinancialContractRegisterInput
import income_management.repositories.FinancialContractRepository
import income_management.{FinancialMovementsProjectionService, RegisterFinancialContractService, ResumeFinancialContractsService}
import infrastructure.reads_and_writes.JodaDateTime
import org.joda.time.DateTime
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pdi.jwt.Jwt
import play.api.libs.json.Json
import play.api.mvc.{Headers, Request, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import utils.builders._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialContractControllerTest extends PlaySpec with Results with MockitoSugar {
  private val registerService = mock[RegisterFinancialContractService]
  private val listService = mock[ResumeFinancialContractsService]
  private val projectionService = mock[FinancialMovementsProjectionService]
  implicit private val repository = mock[FinancialContractRepository]
  private val auth = mock[AuthorizationHelper]

  private val expectedUserId = "an-user-id"
  private val jwtBody = s"""{"sub":"$expectedUserId","iat":1516239022}"""
  private val user: User = User(expectedUserId)

  implicit private val sys = ActorSystem("FinancialContractControllerTest")
  implicit private val mat = ActorMaterializer()

  private val controller = new FinancialContractController(
    Helpers.stubControllerComponents(),
    registerService,
    listService,
    projectionService,
    auth
  )

  "should list contracts from user" in {
    val firstResume = FinancialContractResumeBuilder().build
    val secondResume = FinancialContractResumeBuilder().build
    val request = FakeRequest().withHeaders(Headers(("Authorization", Jwt.encode(jwtBody))))

    when(auth.isLoggedIn[Any](any[Request[Any]])).thenReturn(Future.successful(user))

    when(listService.list(user, 1, 2))
      .thenReturn(Future.successful(Seq(firstResume, secondResume)))

    val result = controller.listIncomeResumes(1, 2).apply(request)

    contentType(result) mustBe Some("application/json")
    status(result) mustEqual  OK
    val jsonContent = contentAsJson(result)

    jsonContent.head("id").as[String] mustEqual firstResume.id
    jsonContent.last("id").as[String] mustEqual secondResume.id
    (jsonContent.head \ "name").as[String] mustEqual firstResume.name
    (jsonContent.last \ "name").as[String] mustEqual secondResume.name
    (jsonContent.head \ "yearlyGrossIncome" \ "valueInCents").as[Long] mustEqual firstResume.yearlyGrossIncome.get.valueInCents
    (jsonContent.last \ "yearlyGrossIncome" \ "valueInCents").as[Long] mustEqual secondResume.yearlyGrossIncome.get.valueInCents
  }

  "should insert an financial contract" in {
    val firstIncomePayload = IncomeRegisterInputBuilder(name = "first income").build
    val firstDiscount = IncomeRegisterDiscountInputBuilder().build
    val discountsInput =s"""
    |[
    |  {
    |   "name": "${firstDiscount.name}",
    |   "discountType": "${firstDiscount.discountType}",
    |   "amount": {
    |     "valueInCents": ${firstDiscount.amount.valueInCents},
    |     "currency": "${firstDiscount.amount.currency}"
    |   }
    |  }
    |]
    |""".stripMargin
    val incomesInput = s"""[
    |{
    |	"name":"${firstIncomePayload.name}",
    |	"incomeType": "${firstIncomePayload.incomeType}",
    |	"amount": {
    |		"valueInCents": ${firstIncomePayload.amount.valueInCents},
    |		"currency": "${firstIncomePayload.amount.currency}"
    |	},
    |	"occurrences": {
    |    "day": ${firstIncomePayload.occurrences.day},
    |    "months": [${firstIncomePayload.occurrences.months.mkString(",")}]
    |  },
    |  "discounts": $discountsInput
    |}
    |]""".stripMargin
    val financialContractInput = s"""{
       |	"name":"New Contract 8",
       |	"contractType": "CLT",
       |	"companyCnpj": "09183746273812",
       |	"startDate": "2010-05-23T21:22:29.758-0300",
       |  "endDate": "2010-05-23T21:22:29.758-0300",
       |  "incomes": $incomesInput
       |}""".stripMargin
    val request = FakeRequest()
      .withMethod("POST")
      .withHeaders(Headers(("Authorization", Jwt.encode(jwtBody))))
      .withBody(Json.parse(financialContractInput))

    val financialContract = FinancialContractBuilder().build
    val income = IncomeBuilder(financialContractId = financialContract.id).build
    val discount = IncomeDiscountBuilder(incomeId = income.id).build

    when(auth.isLoggedIn[Any](any[Request[Any]])).thenReturn(Future.successful(user))
    when(registerService.register(any[FinancialContractRegisterInput])(any[User], any[DateTime]))
      .thenReturn(Future.successful((financialContract, Seq(
        (income, Seq(discount))
      ))))

    val result = controller.registerNewFinancialContract().apply(request)

    contentType(result) mustBe Some("application/json")
    status(result) mustEqual CREATED
    val jsonContent = contentAsJson(result)

    jsonContent("id").as[String] mustEqual financialContract.id
    (jsonContent \ "name").as[String] mustEqual financialContract.name
    (jsonContent \ "companyCnpj").as[String] mustEqual financialContract.companyCnpj.get

    val incomeJson = (jsonContent \ "incomes").head
    incomeJson("id").as[String] mustEqual income.id
    incomeJson("name").as[String] mustEqual income.name
    incomeJson("incomeType").as[String] mustEqual income.incomeType.toString
    (incomeJson \ "amount" \ "valueInCents").as[Long] mustEqual income.amount.valueInCents
    (incomeJson \ "amount" \ "currency").as[String] mustEqual income.amount.currency.toString
    (incomeJson \ "occurrences" \ "day").as[Int] mustEqual income.occurrences.day
    (incomeJson \ "occurrences" \ "months").as[List[Int]] mustEqual income.occurrences.months

    val discountJson = (incomeJson \ "discounts").head
    discountJson("id").as[String] mustEqual discount.id
    discountJson("name").as[String] mustEqual discount.name
    discountJson("discountType").as[String] mustEqual discount.discountType.toString
    (discountJson \ "amount" \ "valueInCents").as[Long] mustEqual discount.amount.valueInCents
    (discountJson \ "amount" \ "currency").as[String] mustEqual discount.amount.currency.toString
  }

  "update an financial contract" in {
    val payload = FinancialContractUpdateInputBuilder().build
    val body = s"""{
     |	"name":"${payload.name}",
     |	"contractType": "${payload.contractType}",
     |	"grossAmount": {
     |		"valueInCents": ${payload.grossAmount.valueInCents},
     |		"currency": "${payload.grossAmount.currency}"
     |	},
     |	"companyCnpj": "${payload.companyCnpj.get}",
     |	"startDate": "${payload.startDate.toString(JodaDateTime.PATTERN)}",
     |	"endDate": "${payload.endDate.get.toString(JodaDateTime.PATTERN)}"
     |}""".stripMargin
    val request = FakeRequest()
      .withMethod("PUT")
      .withBody(Json.parse(body))
      .withHeaders(Headers(("Authorization", Jwt.encode(jwtBody))))

    val financialContract = FinancialContractBuilder(user = user).build

    // Intellij has an error in next line. It is only a highlight error.
    when(auth.authorizeObject[Any](any[String])(any[Request[Any]], any[FinancialContractRepository]))
      .thenReturn(Future.successful(true))
    when(repository.getById(financialContract.id))
      .thenReturn(Future.successful(Some(financialContract)))
    when(repository.update(eqTo(financialContract.id), eqTo(payload), any[DateTime]))
      .thenReturn(Future.successful(1))

    val result = controller.updateFinancialContract(financialContract.id).apply(request)

    contentType(result) mustBe Some("application/json")
    status(result) mustEqual OK
    verify(repository, times(1)).update(financialContract.id, payload)

    val jsonContent = contentAsJson(result)

    jsonContent("id").as[String] mustEqual financialContract.id
    (jsonContent \ "user" \ "id").as[String] mustEqual financialContract.user.id
    (jsonContent \ "name").as[String] mustEqual financialContract.name
    (jsonContent \ "companyCnpj").as[String] mustEqual financialContract.companyCnpj.get
  }

  "delete an user contract" in {
    val request = FakeRequest()
      .withMethod("DELETE")
      .withHeaders(Headers(("Authorization", Jwt.encode(jwtBody))))

    val financialContract = FinancialContractBuilder(user = user).build
    // Intellij has an error in next line. It is only a highlight error.
    when(auth.authorizeObject[Any](any[String])(any[Request[Any]], any[FinancialContractRepository]))
      .thenReturn(Future.successful(true))
    when(repository.delete(eqTo(financialContract.id)))
      .thenReturn(Future.successful(1))

    val result = controller.deleteFinancialContract(financialContract.id).apply(request)

    contentType(result) mustBe None
    status(result) mustEqual NO_CONTENT
    verify(repository, times(1)).delete(financialContract.id)
  }
}
