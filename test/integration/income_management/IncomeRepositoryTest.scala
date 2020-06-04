package integration.income_management

import domain.Income.IncomeType
import domain._
import income_management.IncomeRepository
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomePayloadBuilder}
import utils.{DBUtils, IntegrationSpec}

class IncomeRepositoryTest extends IntegrationSpec {
  private val repository = new IncomeRepository(dbConfig)
  private val now = DateTime.now
  private val financialContract: FinancialContract = FinancialContractBuilder().build
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
  private val forthIncome = IncomeBuilder(
    financialContractId = financialContract.id,
    createdAt = now.minusDays(4),
  ).build
  private val fifthIncome = IncomeBuilder(
    financialContractId = financialContract.id,
    createdAt = now.minusDays(5),
  ).build
  private val firstNoiseIncome = IncomeBuilder(
    financialContractId = noiseFinancialContract.id,
  ).build

  private val financialContractList = List(financialContract, noiseFinancialContract)
  private lazy val incomeList = List(
    fifthIncome,
    firstIncome,
    forthIncome,
    firstNoiseIncome,
    secondIncome,
    thirdIncome
  )

  behavior of "listing incomes"
  it should """return all incomes descending ordered by creation date
              |when on first page with size 3 and there are 3 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(List(firstIncome, firstNoiseIncome, secondIncome, thirdIncome))
      incomes <- repository.allByFinancialContractId(financialContract.id, 1, 3)
    } yield {
      incomes should have length 3

      incomes.map(income => income: Income) shouldEqual List(
        firstIncome,
        secondIncome,
        thirdIncome,
      )
    }
  }

  it should """return the first 2 incomes ordered by creation date
              |when on first page with size 2 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      incomes <- repository.allByFinancialContractId(financialContract.id,1, 2)
    } yield {
      incomes should have length 2

      incomes.map(income => income: Income) shouldEqual List(
        firstIncome,
        secondIncome,
      )
    }
  }

  it should """return 2 incomes ordered by creation date
              |when on second page with size 2 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      incomes <- repository.allByFinancialContractId(financialContract.id,2, 2)
    } yield {
      incomes should have length 2

      incomes.map(income => income: Income) shouldEqual List(
        thirdIncome,
        forthIncome,
      )
    }
  }

  it should """return 1 income when on last page with size 2 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      incomes <- repository.allByFinancialContractId(financialContract.id,3, 2)
    } yield {
      incomes should have length 1

      incomes.map(income => income: Income) shouldEqual List(fifthIncome)
    }
  }

  behavior of "get income by id"
  it should """return the financial contract with the existent id""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      income <- repository.getById(forthIncome.id)
    } yield {
      income.get shouldEqual forthIncome
    }
  }

  it should """return none when financial contract does not exist""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      income <- repository.getById("not-existent-id")
    } yield {
      income shouldEqual None
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

  behavior of "deleting income"
  it should "delete the specified income" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(List(fifthIncome, thirdIncome, firstIncome))
      affectedRows <- repository.delete(thirdIncome.id)
      allIncomes <- DBUtils.allIncomes
    } yield {
      allIncomes should have length 2
      affectedRows shouldEqual 1
      allIncomes.head.id shouldEqual firstIncome.id
      allIncomes.last.id shouldEqual fifthIncome.id
    }
  }

  behavior of "updating income"
  it should "update the allowed fields" in {
    val now = DateTime.now
    val updatePayload = IncomePayloadBuilder(
      name = "Updated Awesome Name",
      amount = Amount(391283, Currency.USD),
      incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE,
      occurrences = Occurrences.builder.day(28).allMonths.build,
    ).build

    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      affectedRows <- repository.update(secondIncome.id, updatePayload, now)
      allIncomes <- DBUtils.allIncomes
    } yield {
      allIncomes should have length 6
      affectedRows shouldEqual 1

      val updatedIncome = allIncomes
        .find(_.id.equals(secondIncome.id))
        .get: Income
      updatedIncome.name shouldEqual updatePayload.name
      updatedIncome.amount.valueInCents shouldEqual updatePayload.amount.valueInCents
      updatedIncome.amount.currency.toString shouldEqual updatePayload.amount.currency
      updatedIncome.incomeType.toString shouldEqual updatePayload.incomeType
      updatedIncome.occurrences shouldEqual updatePayload.occurrences
      updatedIncome.createdAt shouldEqual secondIncome.createdAt
      updatedIncome.modifiedAt shouldEqual now
    }
  }
}
