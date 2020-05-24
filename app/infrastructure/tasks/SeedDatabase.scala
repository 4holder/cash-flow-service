package infrastructure.tasks

import java.sql.Timestamp
import java.util.UUID.randomUUID

import domain.{ContractType, Currency}
import income_management.models.financial_contract.{FinancialContractDbRow, FinancialContractTable}
import infrastructure.tasks.DatabaseConnection._
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.duration._
import scala.concurrent.Await

object SeedDatabase {
  private val userId = "local-user-id"

  def main(args: Array[String]): Unit = {
    val financialContractTable = TableQuery[FinancialContractTable]
    println("Seeding database with contracts. User id = " + userId)

    Await.result(db.run(DBIO.seq(
      financialContractTable += randomFinancialContract,
      financialContractTable += randomFinancialContract,
      financialContractTable += randomFinancialContract,
      financialContractTable += randomFinancialContract,
      financialContractTable += randomFinancialContract,
    )), 10 seconds)
  }

  def randomFinancialContract: FinancialContractDbRow = {
    FinancialContractDbRow(
      id = randomUUID().toString,
      user_id = userId,
      name = s"A Good Contract ${DateTime.now.getMillis}",
      contract_type = ContractType.CLT.toString,
      gross_amount_in_cents = 1235000,
      currency = Currency.BRL.toString,
      is_active = true,
      company_cnpj = Some("3311330900014"),
      start_date = new Timestamp(DateTime.now.getMillis),
      end_date = Some(new Timestamp(DateTime.now.getMillis)),
      created_at = new Timestamp(DateTime.now.getMillis),
      modified_at = new Timestamp(DateTime.now.getMillis)
    )
  }

}
