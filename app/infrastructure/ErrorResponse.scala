package infrastructure

import play.api.libs.json.{Json, Writes}

case class ErrorResponse(
  message: String
)

object ErrorResponse {
  implicit val errorResponseWrites: Writes[ErrorResponse] = Json.writes[ErrorResponse]

  def apply(message: Throwable): ErrorResponse = ErrorResponse(message.getMessage)

  def notFound = ErrorResponse("Resource not found.")
}
