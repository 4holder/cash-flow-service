package integration.income_management

import domain.Income.IncomeType
import domain.IncomeDiscount.IncomeDiscountType
import domain.{Amount, Currency, FinancialContract, Income, IncomeDiscount, Occurrences}
import income_management.IncomeDiscountRepository
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomeDiscountBuilder, IncomeDiscountPayloadBuilder, IncomePayloadBuilder}
import utils.{DBUtils, IntegrationSpec}

class IncomeDiscountRepositoryTest extends IntegrationSpec  {
  private val repository = new IncomeDiscountRepository(dbConfig)
  private val now = DateTime.now
  private val financialContract: FinancialContract = FinancialContractBuilder().build
  private val noiseFinancialContract = FinancialContractBuilder().build
  private val income = IncomeBuilder(financialContractId = financialContract.id).build
  private val noiseIncome = IncomeBuilder(financialContractId = financialContract.id).build
  private val firstIncomeDiscount = IncomeDiscountBuilder(
    incomeId = income.id,
    createdAt = now.minusDays(1),
  ).build
  private val secondIncomeDiscount = IncomeDiscountBuilder(
    incomeId = income.id,
    createdAt = now.minusDays(2),
  ).build
  private val thirdIncomeDiscount = IncomeDiscountBuilder(
    incomeId = income.id,
    createdAt = now.minusDays(3),
  ).build
  private val forthIncomeDiscount = IncomeDiscountBuilder(
    incomeId = income.id,
    createdAt = now.minusDays(4),
  ).build
  private val fifthIncomeDiscount = IncomeDiscountBuilder(
    incomeId = income.id,
    createdAt = now.minusDays(5),
  ).build
  private val firstIncomeDiscountNoise = IncomeDiscountBuilder(
    incomeId = noiseIncome.id
  ).build
  private val secondIncomeDiscountNoise = IncomeDiscountBuilder(
    incomeId = noiseIncome.id
  ).build

  private val financialContractList = List(financialContract, noiseFinancialContract)
  private lazy val incomeList = List(income, noiseIncome)
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
  it should """return all income discounts ordered by creation date descending
              |when on first page with size 5 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscounts <- repository.allByIncomeId(income.id, 1, 5)
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

  it should """return the first 2 income discounts ordered by creation date
              |when on first page with size 2 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscounts <- repository.allByIncomeId(income.id, 1, 2)
    } yield {
      incomeDiscounts should have length 2

      incomeDiscounts.map(income => income: IncomeDiscount) shouldEqual List(
        firstIncomeDiscount,
        secondIncomeDiscount,
      )
    }
  }

  it should """return 2 income discounts ordered by creation date
              |when on second page with size 2 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscounts <- repository.allByIncomeId(income.id, 2, 2)
    } yield {
      incomeDiscounts should have length 2

      incomeDiscounts.map(income => income: IncomeDiscount) shouldEqual List(
        thirdIncomeDiscount,
        forthIncomeDiscount,
      )
    }
  }

  it should """return an income discount ordered by creation date
              |when on last page with size 2 and there are 5 incomes""".stripMargin in {
    for {
      _ <- DBUtils.insertFinancialContracts(financialContractList)
      _ <- DBUtils.insertIncomes(incomeList)
      _ <- DBUtils.insertIncomeDiscounts(incomeDiscountList)
      incomeDiscounts <- repository.allByIncomeId(income.id, 3, 2)
    } yield {
      incomeDiscounts should have length 1

      incomeDiscounts.map(income => income: IncomeDiscount) shouldEqual List(fifthIncomeDiscount)
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
}
