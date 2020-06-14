package authorization

import authorization.exceptions.{InvalidUserTokenException, PermissionDeniedException, UserTokenMissingException}
import com.google.inject.{Inject, Singleton}
import domain.{Repository, User}
import income_management.repositories.FinancialContractRepository
import pdi.jwt.{Jwt, JwtOptions}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@Singleton
class AuthorizationHelper @Inject()(implicit ec: ExecutionContext) extends Logging {
  def isLoggedIn[A](implicit request: Request[A]): Future[User] = {
    Future.fromTry(getUserFromRequest(request))
  }

  def authorizeObject[A](id: String)
                        (implicit request: Request[A], repository: Repository): Future[Boolean] = {
    Future
      .fromTry(getUserFromRequest(request))
      .flatMap(user => {
        repository
          .belongsToUser(id, user) map {
            case true => true
            case false =>
              val failureMessage = s"Object '$id' is not associated with '${user.id}'. Access Denied."
              logger.info(failureMessage)
              throw PermissionDeniedException(failureMessage)
          }
      })
  }

  def authorizeParent[A](id: String)
                        (implicit request: Request[A], repository: Repository): Future[Boolean] = {
    Future
      .fromTry(getUserFromRequest(request))
      .flatMap(user => {
        repository
          .parentBelongsToUser(id, user) map {
          case true => true
          case false =>
            val failureMessage = s"Object '$id' is not associated with '${user.id}'. Access Denied."
            logger.info(failureMessage)
            throw PermissionDeniedException(failureMessage)
        }
      })
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
