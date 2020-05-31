package unit.income_management

import authorization.AuthorizationHelper
import domain.User
import income_management.{IncomeController, IncomeRepository, RegisterIncomeService}
import infrastructure.reads_and_writes.JodaDateTime
import org.joda.time.DateTime
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pdi.jwt.Jwt
import play.api.libs.json.Json
import play.api.mvc.{Headers, Request, Results}
import play.api.test.Helpers.{contentAsJson, contentType, status}
import play.api.test.{FakeRequest, Helpers}
import utils.builders.{FinancialContractBuilder, FinancialContractPayloadBuilder, IncomeBuilder, IncomePayloadBuilder}
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncomeControllerTest extends PlaySpec with Results with MockitoSugar {
  private val service = mock[RegisterIncomeService]
  private val repository = mock[IncomeRepository]
  private val auth = mock[AuthorizationHelper]

  private val expectedUserId = "an-user-id"
  private val jwtToken = Jwt.encode(s"""{"sub":"$expectedUserId","iat":1516239022}""")
  private val user: User = User(expectedUserId)

  private val financialContract = FinancialContractBuilder().build

  private val controller = new IncomeController(
    Helpers.stubControllerComponents(),
    service,
    repository,
    auth,
  )

  "Income Controller" should {
    "list incomes" in {
      val request = FakeRequest().withHeaders(Headers(("Authorization", jwtToken)))

      val firstIncome = IncomeBuilder(financialContractId = financialContract.id).build
      val secondIncome = IncomeBuilder(financialContractId = financialContract.id).build

      when(auth.authorizeByFinancialContract[Any](eqTo(financialContract.id))(any[Request[Any]]))
        .thenReturn(Future.successful(true))

      when(repository.allByFinancialContractId(financialContract.id, 1, 2))
        .thenReturn(Future.successful(
          Seq(
            firstIncome,
            secondIncome
          )
        ))

      val result = controller.listIncomes(financialContract.id, 1, 2).apply(request)

      contentType(result) mustBe Some("application/json")
      status(result) mustEqual OK
      val jsonContent = contentAsJson(result)

      jsonContent.head("id").as[String] mustEqual firstIncome.id
      jsonContent.last("id").as[String] mustEqual secondIncome.id
      (jsonContent.head \ "name").as[String] mustEqual firstIncome.name
      (jsonContent.last \ "name").as[String] mustEqual secondIncome.name
      (jsonContent.head \ "amount" \ "valueInCents").as[Long] mustEqual firstIncome.amount.valueInCents
      (jsonContent.last \ "amount" \ "valueInCents").as[Long] mustEqual secondIncome.amount.valueInCents
      (jsonContent.head \ "amount" \ "currency").as[String] mustEqual firstIncome.amount.currency.toString
      (jsonContent.last \ "amount" \ "currency").as[String] mustEqual secondIncome.amount.currency.toString
    }

    "register new income" in {
      val firstIncomePayload = IncomePayloadBuilder(name = "first income").build
      val secondIncomePayload = IncomePayloadBuilder(name = "second income").build
      val body = s"""[
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
        |  }
        |},
        |{
        |	"name":"${secondIncomePayload.name}",
        |	"incomeType": "${secondIncomePayload.incomeType}",
        |	"amount": {
        |		"valueInCents": ${secondIncomePayload.amount.valueInCents},
        |		"currency": "${secondIncomePayload.amount.currency}"
        |	},
        |	"occurrences": {
        |    "day": ${secondIncomePayload.occurrences.day},
        |    "months": [${secondIncomePayload.occurrences.months.mkString(",")}]
        |  }
        |}
        |]""".stripMargin
      val request = FakeRequest()
        .withMethod("POST")
        .withHeaders(Headers(("Authorization", jwtToken)))
        .withBody(Json.parse(body))

      val now = DateTime.now
      val firstIncome = IncomeBuilder(financialContractId = financialContract.id).build
      val secondIncome = IncomeBuilder(financialContractId = financialContract.id).build

      when(auth.authorizeByFinancialContract[Any](eqTo(financialContract.id))(any[Request[Any]]))
        .thenReturn(Future.successful(true))
      when(service.register(eqTo(financialContract.id), eqTo(firstIncomePayload), any[String], any[DateTime]))
        .thenReturn(Future.successful(firstIncome))
      when(service.register(eqTo(financialContract.id), eqTo(secondIncomePayload), any[String], any[DateTime]))
        .thenReturn(Future.successful(secondIncome))
      when(repository.register(firstIncome))
        .thenReturn(Future.successful())
      when(repository.register(secondIncome))
        .thenReturn(Future.successful())

      val result = controller.registerNewIncome(financialContract.id).apply(request)

      contentType(result) mustBe Some("application/json")
      status(result) mustEqual CREATED
      val jsonContent = contentAsJson(result)

      jsonContent.head("id").as[String] mustEqual firstIncome.id
      jsonContent.last("id").as[String] mustEqual secondIncome.id
      (jsonContent.head \ "name").as[String] mustEqual firstIncome.name
      (jsonContent.last \ "name").as[String] mustEqual secondIncome.name
      (jsonContent.head \ "amount" \ "valueInCents").as[Long] mustEqual firstIncome.amount.valueInCents
      (jsonContent.last \ "amount" \ "valueInCents").as[Long] mustEqual secondIncome.amount.valueInCents
      (jsonContent.head \ "amount" \ "currency").as[String] mustEqual firstIncome.amount.currency.toString
      (jsonContent.last \ "amount" \ "currency").as[String] mustEqual secondIncome.amount.currency.toString
    }

    "delete income" in {
      val request = FakeRequest()
        .withMethod("DELETE")
        .withHeaders(Headers(("Authorization", jwtToken)))

      val income = IncomeBuilder().build

      when(auth.authorizeByFinancialContract[Any](any[String])(any[Request[Any]]))
        .thenReturn(Future.successful(true))
      when(repository.delete(eqTo(income.id)))
        .thenReturn(Future.successful(1))

      val result = controller.deleteIncome(financialContract.id, income.id).apply(request)

      contentType(result) mustBe None
      status(result) mustEqual NO_CONTENT
      verify(repository, times(1)).delete(income.id)
    }

    "update income" in {
      val firstIncomePayload = IncomePayloadBuilder(name = "first income").build
      val body = s"""{
        |	"name":"${firstIncomePayload.name}",
        |	"incomeType": "${firstIncomePayload.incomeType}",
        |	"amount": {
        |		"valueInCents": ${firstIncomePayload.amount.valueInCents},
        |		"currency": "${firstIncomePayload.amount.currency}"
        |	},
        |	"occurrences": {
        |    "day": ${firstIncomePayload.occurrences.day},
        |    "months": [${firstIncomePayload.occurrences.months.mkString(",")}]
        |  }
        |}""".stripMargin
      val request = FakeRequest()
        .withMethod("PUT")
        .withBody(Json.parse(body))
        .withHeaders(Headers(("Authorization", jwtToken)))

      val income = IncomeBuilder().build

      when(auth.authorizeByFinancialContract[Any](any[String])(any[Request[Any]]))
        .thenReturn(Future.successful(true))
      when(repository.getById(income.id)).thenReturn(Future.successful(Some(income)))
      when(repository.update(eqTo(income.id), eqTo(firstIncomePayload), any[DateTime]))
        .thenReturn(Future.successful(1))

      val result = controller.updateIncome(financialContract.id, income.id).apply(request)

      contentType(result) mustBe Some("application/json")
      status(result) mustEqual OK
      verify(repository, times(1)).update(income.id, firstIncomePayload)

      val jsonContent = contentAsJson(result)

      jsonContent("id").as[String] mustEqual income.id
      (jsonContent \ "name").as[String] mustEqual income.name
      (jsonContent \ "amount" \ "valueInCents").as[Long] mustEqual income.amount.valueInCents
      (jsonContent \ "amount" \ "currency").as[String] mustEqual income.amount.currency.toString
    }
  }
}
