package unit.income_management

import domain.Amount
import income_management.DetailFinancialContractService
import income_management.repositories.{FinancialContractRepository, IncomeDiscountRepository, IncomeRepository}
import utils.AsyncUnitSpec
import org.mockito.Mockito._
import utils.builders.{FinancialContractBuilder, IncomeBuilder, IncomeDiscountBuilder}

import scala.concurrent.Future

class DetailFinancialContractServiceTest extends AsyncUnitSpec {
  private val financialContractRepository = mock[FinancialContractRepository]
  private val incomeRepository = mock[IncomeRepository]
  private val incomeDiscountRepository = mock[IncomeDiscountRepository]
  private val service = new DetailFinancialContractService(
    financialContractRepository,
    incomeRepository,
    incomeDiscountRepository
  )
  private val aContract = FinancialContractBuilder().build
  private val salaryIncome = IncomeBuilder(
    financialContractId = aContract.id,
    amount = Amount.BRL(100000),
  ).build
  private val inssDiscount = IncomeDiscountBuilder(
    incomeId = salaryIncome.id,
    amount = Amount.BRL(10000),
  ).build

  behavior of "detailing a CLT contract"
  it should "return none when the contract is not found" in {
    when(financialContractRepository.getById(aContract.id))
      .thenReturn(Future.successful(None))
    when(incomeRepository.allByFinancialContractIds())
      .thenReturn(Future.successful(Seq()))
    when(incomeDiscountRepository.allByIncomeIds(Seq()))
      .thenReturn(Future.successful(Seq()))

    service
      .details(aContract.id)
      .map{contract => contract shouldEqual None}
  }

  it should "have total yearly incomes and discounts equal R$ 0 when there is no income" in {
    when(financialContractRepository.getById(aContract.id))
      .thenReturn(Future.successful(Some(aContract)))
    when(incomeRepository.allByFinancialContractIds(aContract.id))
      .thenReturn(Future.successful(Seq()))
    when(incomeDiscountRepository.allByIncomeIds(Seq()))
      .thenReturn(Future.successful(Seq()))

    val futureContractDetails = service.details(aContract.id)

    futureContractDetails.map{case Some(contractDetails) =>
      contractDetails.totalYearlyGrossAmount shouldEqual Amount.ZERO_REAIS
      contractDetails.totalYearlyDiscountAmount shouldEqual Amount.ZERO_REAIS
      contractDetails.totalYearlyNetAmount shouldEqual Amount.ZERO_REAIS
    }
  }

  it should "gross and net yearly amount are 12000 when there is only a R$1000 monthly payment" in {
    when(financialContractRepository.getById(aContract.id))
      .thenReturn(Future.successful(Some(aContract)))
    when(incomeRepository.allByFinancialContractIds(Seq(aContract.id):_*))
      .thenReturn(Future.successful(Seq(salaryIncome)))
    when(incomeDiscountRepository.allByIncomeIds(Seq(salaryIncome.id)))
      .thenReturn(Future.successful(Seq()))

    val futureContractDetails = service.details(aContract.id)

    futureContractDetails.map{case Some(contractDetails) =>
      contractDetails.totalYearlyGrossAmount shouldEqual Amount.BRL(1200000)
      contractDetails.totalYearlyDiscountAmount shouldEqual Amount.ZERO_REAIS
      contractDetails.totalYearlyNetAmount shouldEqual Amount.BRL(1200000)
    }
  }

  it should
    """gross amount is R$13200,00 and net amount is R$12000,00
      |when there is only a R$1000 monthly payment
      |with a R$100 monthly discount""".stripMargin in {
    when(financialContractRepository.getById(aContract.id))
      .thenReturn(Future.successful(Some(aContract)))
    when(incomeRepository.allByFinancialContractIds(Seq(aContract.id):_*))
      .thenReturn(Future.successful(Seq(salaryIncome)))
    when(incomeDiscountRepository.allByIncomeIds(Seq(salaryIncome.id)))
      .thenReturn(Future.successful(Seq(inssDiscount)))

    val futureContractDetails = service.details(aContract.id)

    futureContractDetails.map{case Some(contractDetails) =>
      contractDetails.totalYearlyGrossAmount shouldEqual Amount.BRL(1320000)
      contractDetails.totalYearlyDiscountAmount shouldEqual Amount.BRL(120000)
      contractDetails.totalYearlyNetAmount shouldEqual Amount.BRL(1200000)
    }
  }
}
