package unit.income_management

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import domain.User
import income_management.FinancialContractController
import income_management.models.financial_contract.FinancialContractRepository
import infrastructure.HealthCheckController
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Headers, Results}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{contentType, status}
import play.api.test.Helpers._
import org.mockito.Mockito
import org.mockito.Mockito._
import pdi.jwt.Jwt
import utils.builders.FinancialContractBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinancialContractControllerTest extends PlaySpec with Results with MockitoSugar {

  private val repository: FinancialContractRepository = mock[FinancialContractRepository]

  private val expectedUserId = "an-user-id"
  private val jwtBody = s"""{"sub":"$expectedUserId","iat":1516239022}"""
  implicit private val user = User(expectedUserId)

  private val controller = new FinancialContractController(
    Helpers.stubControllerComponents(),
    repository
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
}
