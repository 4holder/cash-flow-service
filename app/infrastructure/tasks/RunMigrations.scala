package infrastructure.tasks

import java.io.File

import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import slick.util.AsyncExecutor
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source.fromFile

/**
 * This is a tech debt.
 *
 * This task run migration in a localhost database because there was a challenging setup
 *  of flyway and circleCI postgres db.
 * The better would be find a way to connection flyway container and postgres in the
 *  pipeline steps.
 */
object RunMigrations {
  private val connectionUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
  val db = Database.forURL(
    url = connectionUrl,
    driver = "org.postgresql.Driver",
    executor = AsyncExecutor("test", queueSize=2, minThreads=1, maxConnections=1, maxThreads=1)
  )

  def main(args: Array[String]): Unit = {
    val projectDir = new File(".").getCanonicalPath
    val migrationsFolder = s"$projectDir/migrations/"

    println(s"Running database migrations at '$migrationsFolder'")

    getListOfFiles(migrationsFolder)
      .foreach(sqlFile => {
        println(s"Applying migration '${sqlFile.getName}'")
        val migrationFile = fromFile(sqlFile).mkString

        Await.result(db.run(DBIO.seq(sqlu"#$migrationFile"))
          .map(_ => println(s"Migration '${sqlFile.getName}' applied"))
          .recover {
            case e => println(s"Something went wrong with '${sqlFile.getName}'. ${e.getMessage}")
          },
          10 seconds)
      })
  }

  private def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
}
