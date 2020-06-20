package infrastructure.tasks

import java.sql.Timestamp
import java.util.UUID.randomUUID

import domain.Currency
import domain.FinancialContract.ContractType
import domain.Income.IncomeType
import domain.IncomeDiscount.IncomeDiscountType
import income_management.repositories.FinancialContractRepository.{FinancialContractDbRow, FinancialContractTable}
import income_management.repositories.IncomeDiscountRepository.{IncomeDiscountDbRow, IncomeDiscountTable}
import income_management.repositories.IncomeRepository.{IncomeDbRow, IncomeTable}
import infrastructure.tasks.DatabaseConnection._
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object SeedDatabase {
  private val userId = "local-user-id"
  private val now = new Timestamp(DateTime.now.getMillis)

  def main(args: Array[String]): Unit = {
    val financialContractTable = TableQuery[FinancialContractTable]
    val incomeTable = TableQuery[IncomeTable]
    val incomeDiscountTable = TableQuery[IncomeDiscountTable]

    println("Seeding database with contracts. User id = " + userId)

    val financialContracts = List(randomFinancialContract, randomFinancialContract)
    val incomes = financialContracts.flatMap(fc => List(
      randomIncome(fc),
      randomIncome(fc),
      randomIncome(fc),
    ))

    val discounts = incomes.flatMap(income => List(
      randomIncomeDiscount(income),
      randomIncomeDiscount(income),
    ))

    Await.result(db.run(DBIO.seq(
      financialContracts.map(fc => financialContractTable += fc):_*
    )), 10 seconds)

    Await.result(db.run(DBIO.seq(
      incomes.map(income => incomeTable += income):_*
    )), 10 seconds)

    Await.result(db.run(DBIO.seq(
      discounts.map(discount => incomeDiscountTable += discount):_*
    )), 10 seconds)
  }

  private def randomFinancialContract: FinancialContractDbRow = {
    val id = randomUUID().toString
    println(s"Financial Contract Id: $id")
    FinancialContractDbRow(
      id = id,
      user_id = userId,
      name = s"A Good Contract ${DateTime.now.getMillis}",
      contract_type = ContractType.CLT.toString,
      company_cnpj = Some("3311330900014"),
      start_date = new Timestamp(DateTime.now.getMillis),
      end_date = Some(new Timestamp(DateTime.now.getMillis)),
      created_at = now,
      modified_at = now,
    )
  }

  private def randomIncome(fc: FinancialContractDbRow): IncomeDbRow = {
    val id = randomUUID().toString
    println(s"Income Id: $id")
    IncomeDbRow(
      id,
      financial_contract_id = fc.id,
      name = "A great salary",
      value_in_cents = 1209313,
      currency = Currency.BRL.toString,
      income_type = IncomeType.SALARY.toString,
      occurrences = "5 *",
      created_at = now,
      modified_at = now,
    )
  }

  private def randomIncomeDiscount(fc: IncomeDbRow): IncomeDiscountDbRow = {
    val id = randomUUID().toString
    println(s"Income Discount Id: $id")
    IncomeDiscountDbRow(
      id,
      income_id = fc.id,
      name = "A bad discount",
      value_in_cents = 19313,
      currency = Currency.BRL.toString,
      discount_type = IncomeDiscountType.INSS.toString,
      created_at = now,
      modified_at = now,
    )
  }
}
