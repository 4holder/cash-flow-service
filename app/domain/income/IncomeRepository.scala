package domain.income

import com.google.inject.{Inject, Singleton}
import domain.Repository
import domain.financial_contract.FinancialContract
import domain.income.Income.{IncomePayload, IncomeTable}
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext)
  extends Repository[Income, IncomePayload, FinancialContract] {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val incomeTable = TableQuery[IncomeTable]

  import dbConfig._
  import profile.api._

  def all(page: Int, pageSize: Int)(implicit fc: FinancialContract): Future[Seq[Income]] = {
    val query = incomeTable
        .filter(_.financial_contract_id === fc.id)
        .sortBy(_.created_at.desc)
        .drop(offset(page, pageSize))
        .take(pageSize)

    db.run(query.result)
      .map(_.map(r => r: Income))
  }

  def getById(id: String): Future[Option[Income]] = {
    db.run(
      incomeTable.filter(_.id === id).result
    ).map(r => r.headOption.map(income => income: Income))
  }

  def register(newIncomes: Income*): Future[Unit] = {
    db.run(
      DBIO.seq(
        newIncomes.map(income => incomeTable += income):_*
      )
    )
  }

  def update(id: String,
             incomePayload: IncomePayload,
             now: DateTime = DateTime.now): Future[Int] = ???

  def delete(id: String): Future[Int] = ???
}
