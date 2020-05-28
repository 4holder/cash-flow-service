package integration.domain.income

import domain.Income
import income_management.IncomeRepository
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import utils.builders.{FinancialContractBuilder, IncomeBuilder}
import utils.{DBUtils, IntegrationSpec}

class IncomeRepositoryTest extends IntegrationSpec {
  private val repository = new IncomeRepository(dbConfig)
  private val now = DateTime.now
  private implicit val financialContract = FinancialContractBuilder().build
  private val noiseFinancialContract = FinancialContractBuilder().build
  private val firstIncome = IncomeBuilder(
    financialContractId = financialContract.id,
    createdAt = now.minusDays(1),
  ).build
  private val secondIncome = IncomeBuilder(
    financialContractId = financialContract.id,
    createdAt = now.minusDays(2),
  ).build
  private val thirdIncome = IncomeBuilder(
    financialContractId = financialContract.id,
    createdAt = now.minusDays(3),
  ).build
  private val firstNoiseIncome = IncomeBuilder(
    financialContractId = noiseFinancialContract.id,
  ).build

  behavior of "listing incomes"
  it should """return all incomes descending ordered by creation date
               |when on first page with size 3 and there are 3 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(financialContract, noiseFinancialContract))
      _ <- DBUtils.insertIncomes(List(firstIncome, firstNoiseIncome, secondIncome, thirdIncome))
      incomes <- repository.all(1, 3)
    } yield {
      incomes should have length 3

      incomes.map(income => income: Income) shouldEqual List(
        firstIncome,
        secondIncome,
        thirdIncome,
      )
    }
  }

  behavior of "inserting income"
  it should "insert a list of valid incomes" in {
    val newIncomes = List(firstIncome, secondIncome, thirdIncome)
    for {
      _ <- DBUtils.insertFinancialContracts(List(financialContract))
      _ <- repository.register(newIncomes:_*)
      allIncomes <- DBUtils.allIncomes
    } yield {
      allIncomes should have length 3

      allIncomes.map(income => income: Income) shouldEqual newIncomes
    }
  }

  it should "not insert a list of valid incomes when financial contract does not exist" in {
    val firstIncome = IncomeBuilder().build
    val secondIncome = IncomeBuilder().build
    val thirdIncome = IncomeBuilder().build

    recoverToSucceededIf[PSQLException] {
      repository.register(firstIncome, secondIncome, thirdIncome)
    }
  }

}
