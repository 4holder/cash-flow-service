package wire

import domain.User
import infrastructure.exceptions.{InvalidUserTokenException, UserTokenMissingException}
import pdi.jwt.{Jwt, JwtOptions}
import play.api.libs.json.Json
import play.api.mvc.Request

import scala.concurrent.Future
import scala.util.{Failure, Try}

object AuthorizedUser {
  implicit def getUser[A](implicit request: Request[A]): Future[User] = {
    Future.fromTry(getUserFromRequest(request))
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
