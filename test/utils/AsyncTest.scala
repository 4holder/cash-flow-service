package utils

import org.scalatest.{AsyncFlatSpec, Matchers}
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.JdbcBackend

trait AsyncTest extends AsyncFlatSpec with Matchers with JdbcBackend {
  implicit private lazy val app = new GuiceApplicationBuilder().
    configure(
      "slick.dbs.default.driver" -> "slick.driver.PostgresDriver$",
      "slick.dbs.default.db.driver" -> "org.postgresql.Driver",
      "slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
    ).build

  protected val dbConfig = new DatabaseConfigProvider {
    def get[P <: BasicProfile]: DatabaseConfig[P] = DatabaseConfigProvider.get
  }
}
