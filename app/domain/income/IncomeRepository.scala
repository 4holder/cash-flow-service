package domain.income

import com.google.inject.{Inject, Singleton}
import domain.income.Income.IncomeTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val incomeTable = TableQuery[IncomeTable]

  import dbConfig._
  import profile.api._

  def registerIncomes(newIncomes: Income*): Future[Seq[Income]] = {
    db.run(DBIO.seq(newIncomes.map(income => incomeTable += income):_*)).map(_ => newIncomes)
  }
}
