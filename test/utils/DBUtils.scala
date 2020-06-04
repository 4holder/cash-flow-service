package utils

import domain.{FinancialContract, Income, IncomeDiscount}
import income_management.FinancialContractRepository.{FinancialContractDbRow, FinancialContractTable}
import income_management.IncomeDiscountRepository.IncomeDiscountTable
import income_management.IncomeRepository.{IncomeDbRow, IncomeTable}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

object DBUtils {
  private val connectionUrl = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres"
  val db = Database.forURL(
    url = connectionUrl,
    driver = "org.postgresql.Driver",
    executor = AsyncExecutor("test", queueSize=100, minThreads=1, maxConnections=1, maxThreads=1)
  )
  val financialContractTable = TableQuery[FinancialContractTable]
  val incomeTable = TableQuery[IncomeTable]
  val incomeDiscountTable = TableQuery[IncomeDiscountTable]

  def allFinancialContractsRows: Future[Seq[FinancialContractDbRow]] = {
    db.run(financialContractTable.result)
  }

  def allIncomes: Future[Seq[IncomeDbRow]] = {
    db.run(incomeTable.sortBy(_.created_at.desc).result)
  }

  def insertFinancialContracts(financialContracts: List[FinancialContract]): Future[Unit] = {
    db.run(
      DBIO.seq(
        financialContracts.map(fc => financialContractTable += fc):_*
      )
    )
  }

  def insertIncomes(incomes: List[Income]): Future[Unit] = {
    db.run(
      DBIO.seq(
        incomes.map(fc => incomeTable += fc):_*
      )
    )
  }

  def insertIncomeDiscounts(incomeDiscounts: List[IncomeDiscount]): Future[Unit] = {
    db.run(
      DBIO.seq(
        incomeDiscounts.map(fc => incomeDiscountTable += fc):_*
      )
    )
  }

  def clearDb(): Future[Unit] = {
    db.run(DBIO.seq(
      sqlu"TRUNCATE public.financial_contracts CASCADE"
    ))
  }
}
