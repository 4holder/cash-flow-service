package domain.exceptions

class InvalidCronExpressionException(message: String) extends RuntimeException(message)

object InvalidCronExpressionException {
  def apply(message: String): InvalidCronExpressionException = {
    new InvalidCronExpressionException(message)
  }
}
