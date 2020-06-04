package integration.income_management

import domain.FinancialContract
import income_management.{IncomeDiscountRepository, IncomeRepository}
import org.joda.time.DateTime
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomeDiscountBuilder}
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

  private val financialContractList = List(financialContract, noiseFinancialContract)
  private lazy val incomeList = List(income, noiseIncome)
  private lazy val incomeDiscountList = List(
    firstIncomeDiscount,
    secondIncomeDiscount,
    thirdIncomeDiscount,
    forthIncomeDiscount,
    fifthIncomeDiscount,
  )

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
}
