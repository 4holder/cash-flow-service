package infrastructure.exceptions

class InvalidUserTokenException(message: String) extends RuntimeException(message)

object InvalidUserTokenException {
  def apply(message: String): InvalidUserTokenException = new InvalidUserTokenException(message)
}

