package infrastructure

import play.api.libs.json.{JsPath, Json, JsonValidationError, Writes}

case class ErrorResponse(
  message: String,
  errors: Option[Map[String, String]] = None,
)

object ErrorResponse {
  implicit val errorResponseWrites: Writes[ErrorResponse] = Json.writes[ErrorResponse]

  def apply(exception: Throwable): ErrorResponse = ErrorResponse(exception.getMessage)
  def apply(message: String, validationErrors: Seq[(JsPath, Seq[JsonValidationError])]): ErrorResponse =
    ErrorResponse(
      message,
      Some(validationErrors.map(e => (e._1.toString, e._2.map(_.message).mkString(","))).toMap)
    )

  def notFound = ErrorResponse("Resource not found.")
}
