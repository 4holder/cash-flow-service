package wire

import domain.User
import play.api.libs.json.{Json, Writes}

case class UserPayload(
  id: String
)

object UserPayload {
  implicit val userPayload: Writes[UserPayload] = Json.writes[UserPayload]

  implicit def fromUser(user: User): UserPayload = {
    UserPayload(
      id = user.id
    )
  }
}
