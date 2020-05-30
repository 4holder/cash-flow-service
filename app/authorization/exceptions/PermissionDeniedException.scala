package authorization.exceptions

class PermissionDeniedException(message: String) extends RuntimeException(message)

object PermissionDeniedException {
  def apply(message: String): PermissionDeniedException = new PermissionDeniedException(message)
}
