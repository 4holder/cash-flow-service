package infrastructure

import com.google.inject.{Inject, Singleton}
import domain.{FinancialContract, User}
import income_management.FinancialContractRepository
import infrastructure.exceptions.{InvalidUserTokenException, UserTokenMissingException}
import pdi.jwt.{Jwt, JwtOptions}
import play.api.libs.json.Json
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@Singleton
class AuthorizationService @Inject()(financialContractRepository: FinancialContractRepository)
                                    (implicit ec: ExecutionContext) {
  def authorize[A](implicit request: Request[A]): Future[User] = {
    Future.fromTry(getUserFromRequest(request))
  }

  def authorizeByFinancialContract[A](id: String)
                                     (implicit request: Request[A]): Future[Boolean] = {
    Future
      .fromTry(getUserFromRequest(request))
      .flatMap(user => financialContractRepository.belongsToUser(id, user))
  }

  private def getUserFromRequest[A](request: Request[A]): Try[User] = {
    request
      .headers
      .get("Authorization")
      .map(_.replace("Bearer ", ""))
      .map(token => {
        Jwt.decodeRaw(token, JwtOptions(signature = false))
          .map(claim => {
            val content = Json.parse(claim)
            User((content \ "sub").as[String])
          }) recoverWith {
            case _ => Failure(InvalidUserTokenException("Token is invalid of malformed."))
          }
      }).getOrElse {
        Failure(UserTokenMissingException("Authorization token missing."))
      }
  }
}
