package domain.financial_contract

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import domain.User
import domain.financial_contract.FinancialContract.{FinancialContractDbRow, FinancialContractPayload, FinancialContractTable}
import org.joda.time.DateTime
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

    db.run(query.result)
      .map(_.map(r => r: FinancialContract))
  }

  def getFinancialContractById(id: String)(implicit user: User): Future[Option[FinancialContract]] = {
    db.run(
      financialContracts
        .filter(r => r.id === id && r.user_id === user.id)
        .result
    ).map(r => r.headOption.map(fc => fc: FinancialContract))
  }

  def insertFinancialContract(newFinancialContract: FinancialContract): Future[FinancialContract] = {
    db.run(financialContracts += newFinancialContract)
      .map(_ => newFinancialContract)
  }

  def updateFinancialContract(id: String, financialContract: FinancialContractPayload, now: DateTime = DateTime.now)
                             (implicit user: User): Future[Int] = {
    db.run(
      financialContracts
        .filter(row => row.id === id && row.user_id === user.id )
        .map(fc => (
          fc.name,
          fc.company_cnpj,
          fc.contract_type,
          fc.gross_amount_in_cents,
          fc.currency,
          fc.start_date,
          fc.end_date,
          fc.modified_at,
        ))
        .update((
          financialContract.name,
          financialContract.companyCnpj,
          financialContract.contractType.toString,
          financialContract.grossAmount.valueInCents,
          financialContract.grossAmount.currency.toString,
          new Timestamp(financialContract.startDate.getMillis),
          financialContract.endDate.map(ed => new Timestamp(ed.getMillis)),
          new Timestamp(now.getMillis),
        ))
    )
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
