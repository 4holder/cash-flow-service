package integration.income_management

import domain.IncomeDiscount.IncomeDiscountType
import domain.{Amount, Currency, FinancialContract, IncomeDiscount}
import income_management.{FinancialContractRepository, IncomeDiscountRepository, IncomeRepository}
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import utils.builders._
import utils.{DBUtils, IntegrationSpec}

class IncomeDiscountRepositoryTest extends IntegrationSpec  {
  private val financialContractRepository = new FinancialContractRepository(dbConfig)
  private val incomeRepository = new IncomeRepository(dbConfig, financialContractRepository)
  private val repository = new IncomeDiscountRepository(dbConfig, incomeRepository)
  private val user = UserBuilder().build
  private val now = DateTime.now
  private val financialContract: FinancialContract = FinancialContractBuilder(user = user).build
  private val noiseFinancialContract = FinancialContractBuilder().build
  private val firstIncome = IncomeBuilder(financialContractId = financialContract.id).build
  private val secondIncome = IncomeBuilder(financialContractId = financialContract.id).build
  private val noiseIncome = IncomeBuilder(financialContractId = financialContract.id).build
  private val firstIncomeDiscount = IncomeDiscountBuilder(
    incomeId = firstIncome.id,
    createdAt = now.minusDays(1),
  ).build
  private val secondIncomeDiscount = IncomeDiscountBuilder(
    incomeId = firstIncome.id,
    createdAt = now.minusDays(2),
  ).build
  private val thirdIncomeDiscount = IncomeDiscountBuilder(
    incomeId = secondIncome.id,
    createdAt = now.minusDays(3),
  ).build
  private val forthIncomeDiscount = IncomeDiscountBuilder(
    incomeId = firstIncome.id,
    createdAt = now.minusDays(4),
  ).build
  private val fifthIncomeDiscount = IncomeDiscountBuilder(
    incomeId = secondIncome.id,
    createdAt = now.minusDays(5),
  ).build
  private val firstIncomeDiscountNoise = IncomeDiscountBuilder(
    incomeId = noiseIncome.id
  ).build
  private val secondIncomeDiscountNoise = IncomeDiscountBuilder(
    incomeId = noiseIncome.id
  ).build

  private val financialContractList = List(financialContract, noiseFinancialContract)
  private lazy val incomeList = List(firstIncome, noiseIncome, secondIncome)
  private lazy val incomeDiscountList = List(
    firstIncomeDiscount,
    firstIncomeDiscountNoise,
    secondIncomeDiscount,
    thirdIncomeDiscount,
    forthIncomeDiscount,
    secondIncomeDiscountNoise,
    fifthIncomeDiscount,
  )

  behavior of "listing income discounts"
  it should "return all income discounts of first and second financial contracts" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscounts <- repository.allByIncomeIds(List(firstIncome.id, secondIncome.id))
    } yield {
      incomeDiscounts should have length 5

      incomeDiscounts.map(income => income: IncomeDiscount) shouldEqual List(
        firstIncomeDiscount,
        secondIncomeDiscount,
        thirdIncomeDiscount,
        forthIncomeDiscount,
        fifthIncomeDiscount,
      )
    }
  }

  behavior of "get income discount by id"
  it should """return the financial contract with the existent id""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscount <- repository.getById(thirdIncomeDiscount.id)
    } yield {
      incomeDiscount.get shouldEqual thirdIncomeDiscount
    }
  }

  it should """return none when financial contract does not exist""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscount <- repository.getById("not-existent-id")
    } yield {
      incomeDiscount shouldEqual None
    }
  }

  behavior of "inserting income discounts"
  it should "insert a list of valid income discounts" in {
    val newIncomeDiscounts = List(firstIncomeDiscount, secondIncomeDiscount, thirdIncomeDiscount)
    for {
      _ <- DBUtils.insertFinancialContracts(List(financialContract))
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- repository.register(newIncomeDiscounts:_*)
      allIncomeDiscounts <- DBUtils.allIncomeDiscounts
    } yield {
      allIncomeDiscounts should have length 3

      allIncomeDiscounts.map(incomeDiscount => incomeDiscount: IncomeDiscount) shouldEqual newIncomeDiscounts
    }
  }

  it should "not insert a list of valid income discounts when income does not exist" in {
    recoverToSucceededIf[PSQLException] {
      repository.register(firstIncomeDiscount, secondIncomeDiscount, thirdIncomeDiscount)
    }
  }

  behavior of "deleting income discount"
  it should "delete specified income discount" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(List(firstIncomeDiscount, secondIncomeDiscount, thirdIncomeDiscount))
      result <- repository.delete(secondIncomeDiscount.id)
      allIncomeDiscounts <- DBUtils.allIncomeDiscounts
    } yield {
      allIncomeDiscounts should have length 2

      result shouldEqual 1
    }
  }

  behavior of "updating income discount"
  it should "update the allowed fields" in {
    val now = DateTime.now
    val updatePayload = IncomeDiscountPayloadBuilder(
      name = "Awesome Updated Name",
      amount = Amount(391283, Currency.USD),
      discountType = IncomeDiscountType.INSS,
    ).build

    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      affectedRows <- repository.update(secondIncomeDiscount.id, updatePayload, now)
      allIncomeDiscounts <- DBUtils.allIncomeDiscounts
    } yield {
      allIncomeDiscounts should have length 7
      affectedRows shouldEqual 1

      val updatedIncome = allIncomeDiscounts
        .find(_.id.equals(secondIncomeDiscount.id))
        .get: IncomeDiscount
      updatedIncome.name shouldEqual updatePayload.name
      updatedIncome.amount.valueInCents shouldEqual updatePayload.amount.valueInCents
      updatedIncome.amount.currency.toString shouldEqual updatePayload.amount.currency
      updatedIncome.discountType.toString shouldEqual updatePayload.discountType
      updatedIncome.createdAt shouldEqual secondIncomeDiscount.createdAt
      updatedIncome.modifiedAt shouldEqual now
    }
  }

  behavior of "belongs to"
  it should "NOT belong to an user which does not owns the financial contract" in {
    val anotherUser = UserBuilder().build
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      belongsTo <- repository.belongsToUser(thirdIncomeDiscount.id, anotherUser)
    } yield {
      belongsTo shouldEqual false
    }
  }

  it should "NOT belong when discount is from another user" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      belongsTo <- repository.belongsToUser(noiseIncome.id, user)
    } yield {
      belongsTo shouldEqual false
    }
  }

  it should "belong to an user which owns the financial contract and income" in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      belongsTo <- repository.belongsToUser(thirdIncomeDiscount.id, user)
    } yield {
      belongsTo shouldEqual true
    }
  }
}
