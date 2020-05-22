package income_management.models.financial_contract

import com.google.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FinancialContractRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                           (implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  val financialContracts = TableQuery[FinancialContractTable]

  import dbConfig._
  import profile.api._

  def getFinancialContracts(page: Int, pageSize: Int): Future[Seq[FinancialContract]] = {
    val query =
      financialContracts
        .drop(offset(page, pageSize))
        .take(pageSize)
        .sortBy(_.created_at.desc)

    db.run(query.result).map(_.map(r => r: FinancialContract))
  }

  def insertContract(newFinancialContract: FinancialContract): Future[FinancialContract] = {

    db.run(DBIO.seq(
      financialContracts += newFinancialContract
    )).map(_ => newFinancialContract)
  }

  private def offset(page: Int, pageSize: Int) = {
    if(page <= 1)
      0
    else
      (page - 1) * pageSize
  }
}
