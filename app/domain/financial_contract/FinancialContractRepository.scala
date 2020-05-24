package domain.financial_contract

import com.google.inject.{Inject, Singleton}
import domain.User
import domain.financial_contract.FinancialContract.FinancialContractTable
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialContractRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                           (implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val financialContracts = TableQuery[FinancialContractTable]

  import dbConfig._
  import profile.api._

  def getFinancialContracts(page: Int, pageSize: Int)(implicit user: User): Future[Seq[FinancialContract]] = {
    val query =
      financialContracts
        .filter(r => r.user_id === user.id)
        .sortBy(_.created_at.desc)
        .drop(offset(page, pageSize))
        .take(pageSize)

    db.run(query.result).map(_.map(r => r: FinancialContract))
  }

  def insertContract(newFinancialContract: FinancialContract): Future[FinancialContract] = {
    db.run(DBIO.seq(
      financialContracts += newFinancialContract
    )).map(_ => newFinancialContract)
  }

  def deleteFinancialContract(id: String)(implicit user: User): Future[Int] = {
    db.run(
      financialContracts
        .filter(f => f.id === id && f.user_id === user.id)
        .delete
    )
  }

  private def offset(page: Int, pageSize: Int) = {
    if(page <= 1)
      0
    else
      (page - 1) * pageSize
  }
}