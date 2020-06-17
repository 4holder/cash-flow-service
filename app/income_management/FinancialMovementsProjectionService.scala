package income_management

import com.google.inject.{Inject, Singleton}
import domain._
import income_management.FinancialMovementsProjectionService.{FinancialMovementsProjection, ProjectionPoint}
import income_management.repositories.{FinancialContractRepository, IncomeDiscountRepository, IncomeRepository}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialMovementsProjectionService @Inject()(
  financialContractRepository: FinancialContractRepository,
  incomeRepository: IncomeRepository,
  incomeDiscountRepository: IncomeDiscountRepository
)(implicit ec: ExecutionContext) {
  def project(
    user: User,
    page: Int,
    pageSize: Int)
  (implicit now: DateTime = DateTime.now): Future[Seq[FinancialMovementsProjection]] = {
    for {
      financialContracts <- financialContractRepository.allByUser(user.id, page, pageSize)
      allIncomes <- incomeRepository.allByFinancialContractIds(financialContracts.map(_.id))
      allIncomeDiscounts <- incomeDiscountRepository.allByIncomeIds(allIncomes.map(_.id))
    } yield Seq(
      projectGrossIncome(allIncomes),
      projectNetIncome(allIncomes, allIncomeDiscounts),
      projectDiscounts(allIncomes, allIncomeDiscounts)
    )
  }

  private def projectGrossIncome(incomes: Seq[Income])(implicit now: DateTime) = {
    FinancialMovementsProjection(
      label = "Gross Income",
      currency = Currency.BRL,
      financialMovements = (1 to 12).map(month => {
        val incomeAmounts = incomes
          .filter(_.occurrences.months.contains(month))
          .map(_.amount)

        val amount =
          incomeAmounts
            .fold(Amount.ZERO_REAIS)((c, p) => (c + p).get)

        ProjectionPoint(
          amount,
          beginningOfTheMonth(month),
        )
      })
    )
  }

  private def projectNetIncome(incomes: Seq[Income], discounts: Seq[IncomeDiscount])(implicit now: DateTime) = {
    FinancialMovementsProjection(
      label = "Net Income",
      currency = Currency.BRL,
      financialMovements = (1 to 12).map(month => {
        val monthIncomes = incomes
          .filter(_.occurrences.months.contains(month))

        val monthDiscounts = monthIncomes.flatMap(income => {
          discounts
            .filter(_.incomeId.equals(income.id))
            .map(_.amount)
        }).fold(Amount.ZERO_REAIS)((c, p) => (c + p).get)

        val mountAmount = monthIncomes.map(_.amount).fold(Amount.ZERO_REAIS)((c, p) => (c + p).get)

        ProjectionPoint(
          (mountAmount - monthDiscounts).get,
          beginningOfTheMonth(month),
        )
      })
    )
  }

  private def projectDiscounts(incomes: Seq[Income], discounts: Seq[IncomeDiscount])(implicit now: DateTime) = {
    FinancialMovementsProjection(
      label = "Discounts",
      currency = Currency.BRL,
      financialMovements = (1 to 12).map(month => {
        val monthDiscounts =
          incomes
            .filter(_.occurrences.months.contains(month))
            .flatMap(income => {
              discounts
                .filter(_.incomeId.equals(income.id))
                .map(_.amount)
            })
            .fold(Amount.ZERO_REAIS)((c, p) => (c + p).get)

        ProjectionPoint(
          monthDiscounts,
          beginningOfTheMonth(month),
        )
      })
    )
  }

  private def beginningOfTheMonth(month: Int)(implicit now: DateTime): DateTime =
    now
      .withMonthOfYear(month)
      .withDayOfMonth(5)
      .withTimeAtStartOfDay()
}

object FinancialMovementsProjectionService {
  case class ProjectionPoint(
    amount: Amount,
    dateTime: DateTime
  )

  case class FinancialMovementsProjection(
    label: String,
    currency: Currency.Value,
    financialMovements: Seq[ProjectionPoint],
  )
}
