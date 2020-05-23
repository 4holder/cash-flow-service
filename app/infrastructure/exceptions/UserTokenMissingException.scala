package infrastructure.exceptions

class UserTokenMissingException(message: String) extends RuntimeException(message)

object UserTokenMissingException {
  def apply(message: String): UserTokenMissingException = new UserTokenMissingException(message)
}
