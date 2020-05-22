package utils

import income_management.models.financial_contract.{FinancialContract, FinancialContractDbRow, FinancialContractTable}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

object DBUtils {
  private val connectionUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
  val db = Database.forURL(
    url = connectionUrl,
    driver = "org.postgresql.Driver",
    executor = AsyncExecutor("test", queueSize=2, minThreads=1, maxConnections=1, maxThreads=1)
  )
  val financialContractTable = TableQuery[FinancialContractTable]

  def allFinancialContractsRows: Future[Seq[FinancialContractDbRow]] = {
    db.run(financialContractTable.result)
  }

  def insertFinancialContracts(financialContracts: List[FinancialContract]): Future[Unit] = {
    db.run(
      DBIO.seq(
        financialContracts.map(fc => financialContractTable += fc):_*
      )
    )
  }

  def clearDb(): Future[Unit] = {
    db.run(DBIO.seq(
      sqlu"TRUNCATE public.financial_contracts CASCADE "
    ))
  }
}
