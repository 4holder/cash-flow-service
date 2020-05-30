package authorization.exceptions

class UserTokenMissingException(message: String) extends RuntimeException(message) with AuthorizationException

object UserTokenMissingException {
  def apply(message: String): UserTokenMissingException = new UserTokenMissingException(message)
}
