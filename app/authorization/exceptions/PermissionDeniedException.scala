package authorization.exceptions

class PermissionDeniedException(message: String) extends RuntimeException(message) with AuthorizationException

object PermissionDeniedException {
  def apply(message: String): PermissionDeniedException = new PermissionDeniedException(message)
}
