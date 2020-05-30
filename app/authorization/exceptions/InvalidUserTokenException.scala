package authorization.exceptions

class InvalidUserTokenException(message: String) extends RuntimeException(message) with AuthorizationException

object InvalidUserTokenException {
  def apply(message: String): InvalidUserTokenException = new InvalidUserTokenException(message)
}

