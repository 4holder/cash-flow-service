package unit.income_management.controllers

import authorization.AuthorizationHelper
import income_management.RegisterIncomeService
import income_management.controllers.IncomeController
import income_management.repositories.IncomeRepository
import org.joda.time.DateTime
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pdi.jwt.Jwt
import play.api.libs.json.Json
import play.api.mvc.{Headers, Request, Results}
import play.api.test.Helpers.{contentAsJson, contentType, status, _}
import play.api.test.{FakeRequest, Helpers}
import utils.builders.{IncomeBuilder, IncomePayloadBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncomeControllerTest extends PlaySpec with Results with MockitoSugar {
  private val service = mock[RegisterIncomeService]
  implicit private val repository = mock[IncomeRepository]
  private val auth = mock[AuthorizationHelper]

  private val expectedUserId = "an-user-id"
  private val jwtToken = Jwt.encode(s"""{"sub":"$expectedUserId","iat":1516239022}""")

  private val controller = new IncomeController(
    Helpers.stubControllerComponents(),
    service,
    auth,
  )

  "Income Controller" should {
    "delete income" in {
      val request = FakeRequest()
        .withMethod("DELETE")
        .withHeaders(Headers(("Authorization", jwtToken)))

      val income = IncomeBuilder().build
      // Intellij is point an error in next line. It is a highlight error.
      when(auth.authorizeObject[Any](any[String])(any[Request[Any]], any[IncomeRepository]))
        .thenReturn(Future.successful(true))
      when(repository.delete(eqTo(income.id)))
        .thenReturn(Future.successful(1))

      val result = controller.deleteIncome(income.id).apply(request)

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

      // Intellij is point an error in next line. It is a highlight error.
      when(auth.authorizeObject[Any](any[String])(any[Request[Any]], any[IncomeRepository]))
        .thenReturn(Future.successful(true))
      when(repository.getById(income.id)).thenReturn(Future.successful(Some(income)))
      when(repository.update(eqTo(income.id), eqTo(firstIncomePayload), any[DateTime]))
        .thenReturn(Future.successful(1))

      val result = controller.updateIncome(income.id).apply(request)

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
