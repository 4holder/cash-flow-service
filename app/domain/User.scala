package domain

import play.api.libs.json.{Json, Reads, Writes}

case class User(
  id: String
)

object User {
  case class UserPayload(
    id: String
  )

  object UserPayload {
    trait ReadsAndWrites {
      implicit val userPayloadWrites: Writes[UserPayload] = Json.writes[UserPayload]
      implicit val userPayloadReads: Reads[UserPayload] = Json.reads[UserPayload]
    }

    implicit def fromUser(user: User): UserPayload = {
      UserPayload(
        id = user.id
      )
    }
  }
}
