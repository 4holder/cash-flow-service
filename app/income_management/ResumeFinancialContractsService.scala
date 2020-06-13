package income_management

import com.google.inject.{Inject, Singleton}
import domain._
import income_management.ResumeFinancialContractsService.FinancialContractResume
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class ResumeFinancialContractsService @Inject()(
  financialContractRepository: FinancialContractRepository,
  incomeRepository: IncomeRepository,
  incomeDiscountRepository: IncomeDiscountRepository
)(implicit ec: ExecutionContext) {
  def list(user: User, page: Int, pageSize: Int): Future[Seq[FinancialContractResume]] = {
    for {
      financialContracts <- financialContractRepository.allByUser(user.id, page, pageSize)
      allIncomes <- incomeRepository.allByFinancialContractIds(financialContracts.map(_.id))
      allIncomeDiscounts <- incomeDiscountRepository.allByIncomeIds(allIncomes.map(_.id))
    } yield financialContracts.map { financialContract =>
      val incomes = allIncomes.filter(_.financialContractId.equals(financialContract.id))
      val discounts = allIncomeDiscounts.filter(discount => incomes.map(_.id).contains(discount.incomeId))

      buildResume(financialContract, incomes, discounts)
    }
  }

  private def buildResume(
    financialContract: FinancialContract,
    incomes: Seq[Income],
    discounts: Seq[IncomeDiscount]
  ): FinancialContractResume = {
    val grossAmount = calculateYearlyGrossIncome(incomes)
    val discountAmount = calculateYearlyIncomeDiscount(incomes, discounts)
    val netDiscount = calculateYearlyNetIncome(discountAmount, grossAmount)

    FinancialContractResume(
      id = financialContract.id,
      name = financialContract.name,
      yearlyGrossIncome = grossAmount,
      yearlyIncomeDiscount = discountAmount,
      yearlyNetIncome = netDiscount,
    )
  }

  private def calculateYearlyNetIncome(
    discountAmount: Option[Amount],
    grossAmount: Option[Amount]
  ): Option[Amount] = {
    discountAmount
      .flatMap(d =>
        // To ignore this possible error from mismatch currencies could lead us to a funny issue
        // However we are going to support only BRL for a few months
        // TODO: Once we need to support more currencies, it should be revisited
        grossAmount.flatMap(g => (g - d).toOption))
      .orElse(grossAmount)
  }

  private def calculateYearlyGrossIncome(incomes: Seq[Income]): Option[Amount] =
    Try {
      incomes
        .map(income => Try(income.yearlyGrossAmount))
        .reduce((current, previous) =>
          // To ignore this possible error from mismatch currencies could lead us to a funny issue
          // However we are going to support only BRL for a few months
          // TODO: Once we need to support more currencies, it should be revisited
          for {
            c <- current
            p <- previous
            sum <- c + p
          } yield sum
        )
    }.flatten.toOption

  private def calculateYearlyIncomeDiscount(
    incomes: Seq[Income],
    incomeDiscounts: Seq[IncomeDiscount]
  ): Option[Amount] =
    Try {
      incomeDiscounts
        .map{incomeDiscount =>
          Try(incomes
            .find(_.id.equals(incomeDiscount.incomeId))
            .map(income => incomeDiscount.amount * income.occurrences.months.length)
            .get)
        }
        .reduce((current, previous) =>
          for {
            c <- current
            p <- previous
            sum <- c + p
          } yield sum
        )
    }.flatten.toOption
}

object ResumeFinancialContractsService {
  case class FinancialContractResume(
    id: String,
    name: String,
    yearlyGrossIncome: Option[Amount],
    yearlyNetIncome: Option[Amount],
    yearlyIncomeDiscount: Option[Amount]
  )
}
