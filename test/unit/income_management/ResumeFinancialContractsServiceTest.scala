package unit.income_management

import domain.Income.IncomeType
import domain.{Amount, Occurrences}
import income_management.repositories.{FinancialContractRepository, IncomeDiscountRepository, IncomeRepository}
import income_management.ResumeFinancialContractsService
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import utils.AsyncUnitSpec
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomeDiscountBuilder, UserBuilder}

import scala.concurrent.Future

class ResumeFinancialContractsServiceTest extends AsyncUnitSpec with MockitoSugar {
  private val financialContractRepository = mock[FinancialContractRepository]
  private val incomeRepository = mock[IncomeRepository]
  private val incomeDiscountRepository = mock[IncomeDiscountRepository]
  private val service = new ResumeFinancialContractsService(
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

  private val firstIncomeDiscount = IncomeDiscountBuilder(
    incomeId = salary.id,
    amount = Amount.BRL(5000)
  ).build
  private val secondIncomeDiscount = IncomeDiscountBuilder(
    incomeId = salary.id,
    amount = Amount.BRL(10000),
  ).build

  private val thirdIncomeDiscount = IncomeDiscountBuilder(
    incomeId = thirteenthSalary.id,
    amount = Amount.BRL(5000)
  ).build
  private val forthIncomeDiscount = IncomeDiscountBuilder(
    incomeId = thirteenthSalary.id,
    amount = Amount.BRL(10000),
  ).build

  private val firstFinancialContractIncomes = Seq(salary, thirteenthSalary, thirteenthSalaryAdvance)
  private val firstFinancialContractIncomeDiscounts = Seq(
    firstIncomeDiscount,
    secondIncomeDiscount,
    thirdIncomeDiscount,
    forthIncomeDiscount,
  )

  behavior of "resuming CLT contracts"
  it should "return empty list when there is not financial contract" in {

    when(financialContractRepository.allByUser(anUser.id, 1, 30))
      .thenReturn(Future.successful(Seq()))

    when(incomeRepository.allByFinancialContractIds(Seq()))
      .thenReturn(Future.successful(Seq()))

    when(incomeDiscountRepository.allByIncomeIds(Seq()))
      .thenReturn(Future.successful(Seq()))

    val resumesFuture = service.list(anUser, 1, 30)

    resumesFuture.map { resumes =>
      resumes should have length 0
    }
  }

  it should
    """return yearly net income R$11050,00
      |when the financial contract is CLT
      |with R$1000,00 month salary
      |with  R$150,00 discounts monthly""".stripMargin in {
    when(financialContractRepository.allByUser(anUser.id, 1, 20))
      .thenReturn(Future.successful(Seq(firstFinancialContract)))

    when(incomeRepository.allByFinancialContractIds(Seq(firstFinancialContract.id)))
      .thenReturn(Future.successful(firstFinancialContractIncomes))

    when(incomeDiscountRepository.allByIncomeIds(firstFinancialContractIncomes.map(_.id)))
      .thenReturn(Future.successful(firstFinancialContractIncomeDiscounts))

    val resumesFuture = service.list(anUser, 1, 20)

    resumesFuture.map { resumes =>
      resumes should have length 1

      val cltResume = resumes.head

      cltResume.id shouldEqual firstFinancialContract.id
      cltResume.name shouldEqual firstFinancialContract.name
      cltResume.yearlyGrossIncome shouldEqual Some(Amount.BRL(1300000))
      cltResume.yearlyIncomeDiscount shouldEqual Some(Amount.BRL(195000))
      cltResume.yearlyNetIncome shouldEqual Some(Amount.BRL(1105000))
    }
  }

  it should
    """return yearly net income R$13000,00
      |when the financial contract is CLT
      |with R$1000,00 month salary
      |with no discounts""".stripMargin in {
    when(financialContractRepository.allByUser(anUser.id, 1, 20))
      .thenReturn(Future.successful(Seq(firstFinancialContract)))

    when(incomeRepository.allByFinancialContractIds(Seq(firstFinancialContract.id)))
      .thenReturn(Future.successful(firstFinancialContractIncomes))

    when(incomeDiscountRepository.allByIncomeIds(firstFinancialContractIncomes.map(_.id)))
      .thenReturn(Future.successful(Seq()))

    service
      .list(anUser, 1, 20)
      .map { resumes =>
        resumes should have length 1

        val cltResume = resumes.head

        cltResume.id shouldEqual firstFinancialContract.id
        cltResume.name shouldEqual firstFinancialContract.name
        cltResume.yearlyGrossIncome shouldEqual Some(Amount.BRL(1300000))
        cltResume.yearlyIncomeDiscount shouldEqual None
        cltResume.yearlyNetIncome shouldEqual Some(Amount.BRL(1300000))
      }
  }

  it should "return none yearly net income when there is no income" in {
    when(financialContractRepository.allByUser(anUser.id, 1, 20))
      .thenReturn(Future.successful(Seq(firstFinancialContract)))

    when(incomeRepository.allByFinancialContractIds(Seq(firstFinancialContract.id)))
      .thenReturn(Future.successful(Seq()))

    when(incomeDiscountRepository.allByIncomeIds(firstFinancialContractIncomes.map(_.id)))
      .thenReturn(Future.successful(Seq()))

    service
      .list(anUser, 1, 20)
      .map { resumes =>
        resumes should have length 1

        val cltResume = resumes.head

        cltResume.id shouldEqual firstFinancialContract.id
        cltResume.name shouldEqual firstFinancialContract.name
        cltResume.yearlyGrossIncome shouldEqual None
        cltResume.yearlyIncomeDiscount shouldEqual None
        cltResume.yearlyNetIncome shouldEqual None
      }
  }
}
