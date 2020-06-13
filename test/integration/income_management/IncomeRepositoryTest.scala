package integration.income_management

import domain.Income.IncomeType
import domain._
import income_management.{FinancialContractRepository, IncomeRepository}
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomePayloadBuilder, UserBuilder}
import utils.{DBUtils, IntegrationSpec}

class IncomeRepositoryTest extends IntegrationSpec {
  private val financialContractRepository = new FinancialContractRepository(dbConfig)
  private val repository = new IncomeRepository(dbConfig, financialContractRepository)
  private val now = DateTime.now
  private val user = UserBuilder().build
  private val firstFinancialContract: FinancialContract = FinancialContractBuilder(user = user).build
  private val secondFinancialContract: FinancialContract = FinancialContractBuilder(user = user).build
  private val noiseFinancialContract = FinancialContractBuilder().build
  private val firstIncome = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    createdAt = now.minusDays(1),
  ).build
  private val secondIncome = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    createdAt = now.minusDays(2),
  ).build
  private val thirdIncome = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    createdAt = now.minusDays(3),
  ).build
  private val forthIncome = IncomeBuilder(
    financialContractId = secondFinancialContract.id,
    createdAt = now.minusDays(4),
  ).build
  private val fifthIncome = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    createdAt = now.minusDays(5),
  ).build
  private val firstNoiseIncome = IncomeBuilder(
    financialContractId = noiseFinancialContract.id,
  ).build

  private val financialContractList = List(
    firstFinancialContract,
    secondFinancialContract,
    noiseFinancialContract
  )
  private lazy val incomeList = List(
    fifthIncome,
    firstIncome,
    forthIncome,
    firstNoiseIncome,
    secondIncome,
    thirdIncome
  )

  behavior of "listing incomes"
  it should "return all incomes of first and second financial contracts" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      incomes <- repository.allByFinancialContractIds(
        List(firstFinancialContract.id, secondFinancialContract.id)
      )
    } yield {
      incomes should have length 5

      incomes.map(income => income: Income) shouldEqual List(
        firstIncome,
        secondIncome,
        thirdIncome,
        forthIncome,
        fifthIncome
      )
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
      _ <- DBUtils.insertFinancialContracts(List(firstFinancialContract))
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
      name = "Awesome Updated Name",
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

  behavior of "belongs to"
  it should "NOT belong to an user which does not owns the financial contract" in {
    val anotherUser = UserBuilder().build
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(List(fifthIncome, thirdIncome, firstIncome))
      belongsTo <- repository.belongsToUser(thirdIncome.id, anotherUser)
    } yield {
      belongsTo shouldEqual false
    }
  }

  it should "belong to an user which owns the financial contract" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(List(fifthIncome, thirdIncome, firstIncome))
      belongsTo <- repository.belongsToUser(thirdIncome.id, user)
    } yield {
      belongsTo shouldEqual true
    }
  }

  it should "NOT belong when the incomes is from another user" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(List(fifthIncome, thirdIncome, firstIncome, firstNoiseIncome))
      belongsTo <- repository.belongsToUser(firstNoiseIncome.id, user)
    } yield {
      belongsTo shouldEqual false
    }
  }
}
