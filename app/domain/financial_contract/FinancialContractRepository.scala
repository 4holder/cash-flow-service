package domain.financial_contract

import java.sql.Timestamp
import com.google.inject.{Inject, Singleton}
import domain.financial_contract.FinancialContract.{FinancialContractPayload, FinancialContractTable}
import domain.{Repository, User}
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialContractRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                           (implicit ec: ExecutionContext)
  extends Repository[FinancialContract, FinancialContractPayload, User]{
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val financialContracts = TableQuery[FinancialContractTable]

  import dbConfig._
  import profile.api._

  def all(page: Int, pageSize: Int)(implicit user: User): Future[Seq[FinancialContract]] = {
    val query =
      financialContracts
        .filter(r => r.user_id === user.id)
        .sortBy(_.created_at.desc)
        .drop(offset(page, pageSize))
        .take(pageSize)

    db.run(query.result)
      .map(_.map(r => r: FinancialContract))
  }

  def getById(id: String): Future[Option[FinancialContract]] = {
    db.run(
      financialContracts
        .filter(r => r.id === id)
        .result
    ).map(r => r.headOption.map(fc => fc: FinancialContract))
  }

  def register(newFinancialContracts: FinancialContract*): Future[Unit] = {
    db.run(
      DBIO.seq(
        newFinancialContracts
          .map(newFC => financialContracts += newFC):_*
      )
    )
  }

  def update(id: String,
             financialContract: FinancialContractPayload,
             now: DateTime = DateTime.now): Future[Int] = {
    db.run(
      financialContracts
        .filter(row => row.id === id)
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

  def delete(id: String): Future[Int] = {
    db.run(
      financialContracts
        .filter(f => f.id === id)
        .delete
    )
  }
}
