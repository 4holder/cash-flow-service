package unit.infrastructure

import authorization.AuthorizationHelper
import income_management.FinancialContractRepository
import authorization.exceptions.{InvalidUserTokenException, UserTokenMissingException}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterEach, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import pdi.jwt.Jwt
import play.api.mvc.{AnyContent, Headers, Request}

class AuthorizationHelperTest extends AsyncFlatSpec with Matchers with MockitoSugar with BeforeAndAfterEach {
  implicit private val request: Request[AnyContent] = mock[Request[AnyContent]]

  private val repository = mock[FinancialContractRepository]
  private val auth = new AuthorizationHelper(repository)

  override def beforeEach {
    Mockito.reset(request)
  }

  behavior of "extraction user info from jwt token"
  it should "extract user id from a valid token" in {
    val expectedUserId = "an-user-id"
    val jwtBody = s"""{"sub":"$expectedUserId","iat":1516239022}"""
    when(request.headers).thenReturn(Headers(("Authorization", Jwt.encode(jwtBody))))

    auth.authorize map { user =>
      user.id shouldEqual expectedUserId
    }
  }

  it should "fail with user token missing when it isn't provided" in {
    when(request.headers).thenReturn(Headers())

    recoverToSucceededIf[UserTokenMissingException] {
      auth.authorize
    }
  }

  it should "fail when token has no sub" in {
    val jwtBody = """{"iat":1516239022}"""
    when(request.headers).thenReturn(Headers(("Authorization", Jwt.encode(jwtBody))))

    recoverToSucceededIf[InvalidUserTokenException] {
      auth.authorize
    }
  }

  it should "fail when an invalid jwt is sent" in {
    when(request.headers).thenReturn(Headers(("Authorization", "any string")))

    recoverToSucceededIf[InvalidUserTokenException] {
      auth.authorize
    }
  }
}
