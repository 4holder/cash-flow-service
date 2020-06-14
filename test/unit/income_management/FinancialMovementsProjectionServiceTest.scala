package unit.income_management

import domain.Income.IncomeType
import domain.IncomeDiscount.IncomeDiscountType
import domain.{Amount, Currency, Occurrences}
import income_management.FinancialMovementsProjectionService
import income_management.repositories.{FinancialContractRepository, IncomeDiscountRepository, IncomeRepository}
import org.joda.time.DateTime
import org.mockito.Mockito._
import utils.AsyncUnitSpec
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomeDiscountBuilder, UserBuilder}

import scala.concurrent.Future

class FinancialMovementsProjectionServiceTest extends AsyncUnitSpec {
  private val financialContractRepository = mock[FinancialContractRepository]
  private val incomeRepository = mock[IncomeRepository]
  private val incomeDiscountRepository = mock[IncomeDiscountRepository]
  private val service = new FinancialMovementsProjectionService(
    financialContractRepository,
    incomeRepository,
    incomeDiscountRepository
  )

  private val anUser = UserBuilder().build

  private val firstFinancialContract = FinancialContractBuilder().build
  private val salary = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    amount = Amount.BRL(100000),
    occurrences = Occurrences.builder.day(5).allMonths.build,
    incomeType = IncomeType.SALARY
  ).build
  private val thirteenthSalary = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    amount = Amount.BRL(50000),
    occurrences = Occurrences.builder.day(20).month(12).build,
    incomeType = IncomeType.THIRTEENTH_SALARY
  ).build
  private val thirteenthSalaryAdvance = IncomeBuilder(
    financialContractId = firstFinancialContract.id,
    amount = Amount.BRL(50000),
    occurrences = Occurrences.builder.day(20).month(11).build,
    incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE
  ).build

  private val salaryINSSDiscount = IncomeDiscountBuilder(
    incomeId = salary.id,
    amount = Amount.BRL(5000),
    discountType = IncomeDiscountType.INSS,
  ).build
  private val salaryIRRFDiscount = IncomeDiscountBuilder(
    incomeId = salary.id,
    amount = Amount.BRL(10000),
    discountType = IncomeDiscountType.IRRF,
  ).build

  private val thirteenthSalaryINSSDiscount = IncomeDiscountBuilder(
    incomeId = thirteenthSalary.id,
    amount = Amount.BRL(5000),
    discountType = IncomeDiscountType.INSS,
  ).build
  private val thirteenthSalaryIRRFDiscount = IncomeDiscountBuilder(
    incomeId = thirteenthSalary.id,
    amount = Amount.BRL(10000),
    discountType = IncomeDiscountType.IRRF,
  ).build

  private val firstFinancialContractIncomes = Seq(salary, thirteenthSalary, thirteenthSalaryAdvance)
  private val firstFinancialContractIncomeDiscounts = Seq(
    salaryINSSDiscount,
    salaryIRRFDiscount,
    thirteenthSalaryINSSDiscount,
    thirteenthSalaryIRRFDiscount,
  )

  private val monthsOfYear: Seq[Int] = Seq(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
  )

  implicit private val now: DateTime = DateTime.now

  behavior of "financial movement projection"
  it should "return empty list when there is no financial contract" in {
    val emptyResponse = Future.successful(Seq())
    when(financialContractRepository.allByUser(anUser.id, 1, 10))
      .thenReturn(emptyResponse)
    when(incomeRepository.allByFinancialContractIds(Seq()))
      .thenReturn(emptyResponse)
    when(incomeDiscountRepository.allByIncomeIds(Seq()))
      .thenReturn(emptyResponse)

    val projectionsFuture = service.project(anUser, 1, 10)

    projectionsFuture.map{projections =>
      projections should have length 3

      val grossIncomeProjection = projections.find(_.label.equals("Gross Income")).get

      grossIncomeProjection.currency shouldEqual Currency.BRL
      grossIncomeProjection
        .financialMovements
        .map(_.amount) should have length 12
      grossIncomeProjection
        .financialMovements
        .map(_.amount)
        .reduce((c, p) => (c + p).get) shouldEqual Amount.ZERO_REAIS
      grossIncomeProjection
        .financialMovements
        .map(_.dateTime.getYear)
        .forall(_.equals(now.getYear)) shouldEqual true

      grossIncomeProjection
        .financialMovements
        .map(_.dateTime.getMonthOfYear()) shouldEqual monthsOfYear


      val netIncomeProjection = projections.find(_.label.equals("Net Income")).get

      netIncomeProjection.currency shouldEqual Currency.BRL
      netIncomeProjection
        .financialMovements
        .map(_.amount) should have length 12
      netIncomeProjection
        .financialMovements
        .map(_.amount)
        .reduce((c, p) => (c + p).get) shouldEqual Amount.ZERO_REAIS

      netIncomeProjection
        .financialMovements
        .map(_.dateTime.getYear)
        .forall(_.equals(now.getYear)) shouldEqual true

      netIncomeProjection
        .financialMovements
        .map(_.dateTime.getMonthOfYear()) shouldEqual monthsOfYear

      val discountsProjection = projections.find(_.label.equals("Discounts")).get

      discountsProjection.currency shouldEqual Currency.BRL
      discountsProjection
        .financialMovements
        .map(_.amount) should have length 12
      discountsProjection
        .financialMovements
        .map(_.amount)
        .reduce((c, p) => (c + p).get) shouldEqual Amount.ZERO_REAIS

      discountsProjection
        .financialMovements
        .map(_.dateTime.getYear)
        .forall(_.equals(now.getYear)) shouldEqual true

      discountsProjection
        .financialMovements
        .map(_.dateTime.getMonthOfYear()) shouldEqual monthsOfYear
    }
  }

  it should
    """return monthly salary incomes with its associated discounts,
      |the thirteen salary and the thirteen salary advance""".stripMargin in {
    when(financialContractRepository.allByUser(anUser.id, 1, 10))
      .thenReturn(Future.successful(Seq(firstFinancialContract)))
    when(incomeRepository.allByFinancialContractIds(Seq(firstFinancialContract.id)))
      .thenReturn(Future.successful(firstFinancialContractIncomes))
    when(incomeDiscountRepository.allByIncomeIds(firstFinancialContractIncomes.map(_.id)))
      .thenReturn(Future.successful(firstFinancialContractIncomeDiscounts))

    val projectionsFuture = service.project(anUser, 1, 10)

    projectionsFuture.map { projections =>
      projections should have length 3

      val grossIncomeProjection = projections.find(_.label.equals("Gross Income")).get

      grossIncomeProjection.currency shouldEqual Currency.BRL
      grossIncomeProjection.financialMovements should have length 12
      grossIncomeProjection
        .financialMovements
        .map(f => (f.dateTime.getMonthOfYear, f.amount)) shouldEqual List(
        (1, salary.amount), //jan
        (2, salary.amount), //feb
        (3, salary.amount), //mars
        (4, salary.amount), //apr
        (5, salary.amount), //may
        (6, salary.amount), //jun
        (7, salary.amount), //jul
        (8, salary.amount), //aug
        (9, salary.amount), //sep
        (10, salary.amount), //oct
        (11, (salary.amount + thirteenthSalaryAdvance.amount).get), //nov
        (12, (salary.amount + thirteenthSalary.amount).get), //dec
      )

      val netIncomeProjection = projections.find(_.label.equals("Net Income")).get

      netIncomeProjection.currency shouldEqual Currency.BRL
      netIncomeProjection.financialMovements should have length 12
      val expectedMonthlyNetIncome = (salary.amount subtract(salaryINSSDiscount.amount, salaryIRRFDiscount.amount)).get
      val expectedNetThirteenSalary = (thirteenthSalary.amount subtract(thirteenthSalaryINSSDiscount.amount, thirteenthSalaryIRRFDiscount.amount)).get
      netIncomeProjection
        .financialMovements
        .map(f => (f.dateTime.getMonthOfYear, f.amount)) shouldEqual List(
        (1, expectedMonthlyNetIncome), //jan
        (2, expectedMonthlyNetIncome), //feb
        (3, expectedMonthlyNetIncome), //mars
        (4, expectedMonthlyNetIncome), //apr
        (5, expectedMonthlyNetIncome), //may
        (6, expectedMonthlyNetIncome), //jun
        (7, expectedMonthlyNetIncome), //jul
        (8, expectedMonthlyNetIncome), //aug
        (9, expectedMonthlyNetIncome), //sep
        (10, expectedMonthlyNetIncome), //oct
        (11, (expectedMonthlyNetIncome + thirteenthSalaryAdvance.amount).get), //nov
        (12, (expectedMonthlyNetIncome + expectedNetThirteenSalary).get), //dec
      )

      val discountsProjection = projections.find(_.label.equals("Discounts")).get

      discountsProjection.currency shouldEqual Currency.BRL
      discountsProjection.financialMovements should have length 12

      val monthlyDiscount = (salaryINSSDiscount.amount + salaryIRRFDiscount.amount).get
      val decemberDiscount = (thirteenthSalaryINSSDiscount.amount + thirteenthSalaryIRRFDiscount.amount).get
      discountsProjection
        .financialMovements
        .map(f => (f.dateTime.getMonthOfYear, f.amount)) shouldEqual List(
        (1, monthlyDiscount), //jan
        (2, monthlyDiscount), //feb
        (3, monthlyDiscount), //mars
        (4, monthlyDiscount), //apr
        (5, monthlyDiscount), //may
        (6, monthlyDiscount), //jun
        (7, monthlyDiscount), //jul
        (8, monthlyDiscount), //aug
        (9, monthlyDiscount), //sep
        (10, monthlyDiscount), //oct
        (11, monthlyDiscount), //nov
        (12, (monthlyDiscount + decemberDiscount).get), //dec
      )
    }
  }
}
