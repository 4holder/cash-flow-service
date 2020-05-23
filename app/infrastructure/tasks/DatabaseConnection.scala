package infrastructure.tasks

import slick.jdbc.PostgresProfile.api._
import slick.util.AsyncExecutor

object DatabaseConnection {
  private val connectionUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
  val db = Database.forURL(
    url = connectionUrl,
    driver = "org.postgresql.Driver",
    executor = AsyncExecutor("test", queueSize=2, minThreads=1, maxConnections=1, maxThreads=1)
  )
}
