package utils.builders

import java.util.UUID.randomUUID

import domain.User

case class UserBuilder(
  id: String = randomUUID().toString
) {
  def build: User = User(
    id = id
  )
}
