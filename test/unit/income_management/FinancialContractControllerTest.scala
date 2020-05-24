package unit.income_management

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import domain.User
import domain.financial_contract.FinancialContract.FinancialContractPayload
import domain.financial_contract.FinancialContractRepository
import income_management.{FinancialContractController, RegisterFinancialContractService}
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import pdi.jwt.Jwt
import play.api.libs.json.Json
import play.api.mvc.{Headers, Results}
import play.api.test.Helpers.{contentType, status, _}
import play.api.test.{FakeRequest, Helpers}
import utils.builders.FinancialContractBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialContractControllerTest extends PlaySpec with Results with MockitoSugar {
  private val service = mock[RegisterFinancialContractService]
  private val repository = mock[FinancialContractRepository]

  private val expectedUserId = "an-user-id"
  private val jwtBody = s"""{"sub":"$expectedUserId","iat":1516239022}"""
  implicit private val user: User = User(expectedUserId)

  implicit private val sys = ActorSystem("MyTest")
  implicit private val mat = ActorMaterializer()

  private val controller = new FinancialContractController(
    Helpers.stubControllerComponents(),
    repository,
    service
  )

  "should list contracts from user" in {
    val request = FakeRequest().withHeaders(Headers(("Authorization", Jwt.encode(jwtBody))))

    val firstFinancialContract = FinancialContractBuilder(user = user).build
    val secondFinancialContract = FinancialContractBuilder(user = user).build
    when(repository.getFinancialContracts(1, 2)(user))
      .thenReturn(Future.successful(
        Seq(
          firstFinancialContract,
          secondFinancialContract
        )
      ))

    val result = controller.listFinancialContracts(1, 2).apply(request)

    contentType(result) mustBe Some("application/json")
    status(result) mustEqual  OK
    val jsonContent = contentAsJson(result)

    jsonContent.head("id").as[String] mustEqual firstFinancialContract.id
    jsonContent.last("id").as[String] mustEqual secondFinancialContract.id
    (jsonContent.head \ "user" \ "id").as[String] mustEqual firstFinancialContract.user.id
    (jsonContent.last \ "user" \ "id").as[String] mustEqual firstFinancialContract.user.id
  }

  "should insert an user contract" in {
    val body = """{
       |	"name":"New Contract 8",
       |	"contractType": "CLT",
       |	"grossAmount": {
       |		"valueInCents": 1100000,
       |		"currency": "BRL"
       |	},
       |	"companyCnpj": "09183746273812",
       |	"startDate": "2010-05-23T21:22:29.758-0300"
       |}""".stripMargin
    val request = FakeRequest()
      .withMethod("POST")
      .withHeaders(Headers(("Authorization", Jwt.encode(jwtBody))))
      .withBody(Json.parse(body))

    val financialContract = FinancialContractBuilder(user = user).build

    when(service.register(any[FinancialContractPayload], any[String], any[DateTime])(any[User]))
      .thenReturn(Future.successful(financialContract))

    val result = controller.registerNewFinancialContract().apply(request)

    contentType(result) mustBe Some("application/json")
    status(result) mustEqual OK
    val jsonContent = contentAsJson(result)

    jsonContent("id").as[String] mustEqual financialContract.id
    (jsonContent \ "user" \ "id").as[String] mustEqual financialContract.user.id
    (jsonContent \ "name").as[String] mustEqual financialContract.name
    (jsonContent \ "companyCnpj").as[String] mustEqual financialContract.companyCnpj.get
    (jsonContent \ "grossAmount" \ "valueInCents").as[Long] mustEqual financialContract.grossAmount.valueInCents
    (jsonContent \ "grossAmount" \ "currency").as[String] mustEqual financialContract.grossAmount.currency.toString
  }
}
