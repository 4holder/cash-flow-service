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

  implicit private val now: DateTime = DateTime.now

  private val monthsOfYear: Seq[Int] = Seq(
    now.plusMonths(1).getMonthOfYear,
    now.plusMonths(2).getMonthOfYear,
    now.plusMonths(3).getMonthOfYear,
    now.plusMonths(4).getMonthOfYear,
    now.plusMonths(5).getMonthOfYear,
    now.plusMonths(6).getMonthOfYear,
    now.plusMonths(7).getMonthOfYear,
    now.plusMonths(8).getMonthOfYear,
    now.plusMonths(9).getMonthOfYear,
    now.plusMonths(10).getMonthOfYear,
    now.plusMonths(11).getMonthOfYear,
    now.plusMonths(12).getMonthOfYear,
  )

  behavior of "financial movement projection"
  it should "return empty list when there is no financial contract" in {
    implicit val now: DateTime = DateTime.now
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
        .forall(v => v.equals(now.getYear) || v.equals(now.getYear + 1)) shouldEqual true

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
        .forall(v => v.equals(now.getYear) || v.equals(now.getYear + 1)) shouldEqual true

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
        .forall(v => v.equals(now.getYear) || v.equals(now.getYear + 1)) shouldEqual true

      discountsProjection
        .financialMovements
        .map(_.dateTime.getMonthOfYear()) shouldEqual monthsOfYear
    }
  }

  it should
    """return monthly salary incomes with its associated discounts,
      |the thirteen salary and the thirteen salary advance
      |for the next 12 months""".stripMargin in {
    implicit val now: DateTime = DateTime.now
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
        .map(f => (f.dateTime.getMonthOfYear, f.amount))
        .zipWithIndex.map(p => {
          val month = p._1._1
          val amount = p._1._2
          val index = p._2
          if (month == 11) {
            amount shouldEqual (salary.amount + thirteenthSalaryAdvance.amount).get
          } else if (month == 12) {
            amount shouldEqual (salary.amount + thirteenthSalary.amount).get
          } else {
            amount shouldEqual salary.amount
            month shouldEqual now.plusMonths(index + 1).getMonthOfYear
          }
        })

      val netIncomeProjection = projections.find(_.label.equals("Net Income")).get

      netIncomeProjection.currency shouldEqual Currency.BRL
      netIncomeProjection.financialMovements should have length 12
      val expectedMonthlyNetIncome = (salary.amount subtract(salaryINSSDiscount.amount, salaryIRRFDiscount.amount)).get
      val expectedNetThirteenSalary = (thirteenthSalary.amount subtract(thirteenthSalaryINSSDiscount.amount, thirteenthSalaryIRRFDiscount.amount)).get
      netIncomeProjection
        .financialMovements
        .map(f => (f.dateTime.getMonthOfYear, f.amount))
        .zipWithIndex.map(p => {
          val month = p._1._1
          val amount = p._1._2
          val index = p._2
          if (month == 11) {
            amount shouldEqual (expectedMonthlyNetIncome + thirteenthSalaryAdvance.amount).get
          } else if (month == 12) {
            amount shouldEqual (expectedMonthlyNetIncome + expectedNetThirteenSalary).get
          } else {
            amount shouldEqual expectedMonthlyNetIncome
            month shouldEqual now.plusMonths(index + 1).getMonthOfYear
          }
        })

      val discountsProjection = projections.find(_.label.equals("Discounts")).get

      val monthlyDiscount = (salaryINSSDiscount.amount + salaryIRRFDiscount.amount).get
      val decemberDiscount = (thirteenthSalaryINSSDiscount.amount + thirteenthSalaryIRRFDiscount.amount).get
      discountsProjection
        .financialMovements
        .map(f => (f.dateTime.getMonthOfYear, f.amount))
        .zipWithIndex.map(p => {
          val month = p._1._1
          val amount = p._1._2
          val index = p._2
          if (month == 12) {
            amount shouldEqual (monthlyDiscount + decemberDiscount).get
          } else {
            amount shouldEqual monthlyDiscount
            month shouldEqual now.plusMonths(index + 1).getMonthOfYear
          }
        })

      discountsProjection.currency shouldEqual Currency.BRL
      discountsProjection.financialMovements should have length 12
    }
  }
}
