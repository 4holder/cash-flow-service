package utils

import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, BeforeAndAfterEach, Matchers}
import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import slick.basic.{BasicProfile, DatabaseConfig}
import slick.jdbc.JdbcBackend

trait IntegrationSpec extends AsyncFlatSpec
  with Matchers
  with JdbcBackend
  with BeforeAndAfter
  with BeforeAndAfterEach {
  implicit private lazy val app: Application = new GuiceApplicationBuilder().
    configure(
      "slick.dbs.default.driver" -> "slick.driver.PostgresDriver$",
      "slick.dbs.default.db.driver" -> "org.postgresql.Driver",
      "slick.dbs.default.db.url" -> "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
    ).build

  protected val dbConfig: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile]: DatabaseConfig[P] = DatabaseConfigProvider.get
  }

  after {
    DBUtils.clearDb()
  }

  override def beforeEach {
    DBUtils.clearDb()
  }
}
