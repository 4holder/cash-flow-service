package integration.domain.income

import domain.income.{Income, IncomeRepository}
import org.postgresql.util.PSQLException
import utils.builders.{FinancialContractBuilder, IncomeBuilder}
import utils.{DBUtils, IntegrationSpec}

class IncomeRepositoryTest extends IntegrationSpec {
  private val repository = new IncomeRepository(dbConfig)

  behavior of "inserting income"
  it should "insert a list of valid incomes" in {
    val financialContract = FinancialContractBuilder().build
    val firstIncome = IncomeBuilder(financialContractId = financialContract.id).build
    val secondIncome = IncomeBuilder(financialContractId = financialContract.id).build
    val thirdIncome = IncomeBuilder(financialContractId = financialContract.id).build

    for {
      _ <- DBUtils.insertFinancialContracts(List(financialContract))
      registeredIncomes <- repository.registerIncomes(firstIncome, secondIncome, thirdIncome)
      allIncomes <- DBUtils.allIncomes
    } yield {
      allIncomes should have length 3

      allIncomes.map(income => income: Income) shouldEqual registeredIncomes
    }
  }

  it should "not insert a list of valid incomes when financial contract does not exist" in {
    val firstIncome = IncomeBuilder().build
    val secondIncome = IncomeBuilder().build
    val thirdIncome = IncomeBuilder().build

    recoverToSucceededIf[PSQLException] {
      repository.registerIncomes(firstIncome, secondIncome, thirdIncome)
    }
  }

}
