package income_management

import com.google.inject.{Inject, Singleton}
import domain.Amount.ZERO_REAIS
import domain.FinancialContract.ContractType
import domain.Income.IncomeType
import domain.IncomeDiscount.IncomeDiscountType
import domain._
import income_management.DetailFinancialContractService.{
  DetailedFinancialContract,
  DetailedIncome,
  DetailedIncomeDiscount,
}
import income_management.repositories.{
  FinancialContractRepository,
  IncomeDiscountRepository,
  IncomeRepository,
}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DetailFinancialContractService @Inject()(
  financialContractRepository: FinancialContractRepository,
  incomeRepository: IncomeRepository,
  incomeDiscountRepository: IncomeDiscountRepository
)(implicit ec: ExecutionContext) {

  def details(financialContractId: String): Future[Option[DetailedFinancialContract]] = {
    for {
      contract <- financialContractRepository.getById(financialContractId)
      incomes <- getIncomesByContract(contract)
      discounts <- getDiscountsByIncomes(incomes)
    } yield buildDetailedFinancialContract(contract, incomes, discounts)

  }

  private def buildDetailedFinancialContract(
    maybeContract: Option[FinancialContract],
    incomes: Seq[Income],
    discounts: Seq[IncomeDiscount]
  ) = {
    maybeContract.map(contract => {
      val yearlyDiscountAmount = calculateYearlyDiscountAmount(incomes, discounts)
      val yearlyNetAmount = calculateNetYearlyAmount(incomes)
      val yearlyGrossAmount = (yearlyNetAmount + yearlyDiscountAmount).getOrElse(ZERO_REAIS)

      DetailedFinancialContract(
        id = contract.id,
        name = contract.name,
        contractType = contract.contractType,
        companyCnpj = contract.companyCnpj,
        incomes = incomes.map(income => {
          DetailedIncome(
            id = income.id,
            name = income.name,
            grossAmount = calculateIncomeGrossAmount(discounts, income),
            netAmount = income.amount,
            incomeType = income.incomeType,
            occurrences = income.occurrences,
            discounts = discounts.filter(_.incomeId.equals(income.id)).map(discount => {
              DetailedIncomeDiscount(
                id = discount.id,
                name = discount.name,
                discountType = discount.discountType,
                amount = discount.amount,
                createdAt = discount.createdAt,
                modifiedAt = discount.modifiedAt,
              )
            }),
            createdAt = income.createdAt,
            modifiedAt = income.modifiedAt,
          )
        }),
        totalYearlyGrossAmount = yearlyGrossAmount,
        totalYearlyNetAmount = yearlyNetAmount,
        totalYearlyDiscountAmount = yearlyDiscountAmount,
        startDate = contract.startDate,
        endDate = contract.endDate,
        createdAt = contract.createdAt,
        modifiedAt = contract.modifiedAt
      )
    })
  }

  private def calculateNetYearlyAmount(incomes: Seq[Income]) = {
    incomes
      .map(income => income.amount * income.occurrences.months.length)
      .fold(ZERO_REAIS)((p, c) => (p + c).getOrElse(ZERO_REAIS))
  }

  def calculateYearlyDiscountAmount(
    incomes: Seq[Income],
    discounts: Seq[IncomeDiscount]
  ): Amount = {
    discounts.map(discount => {
      incomes
        .find(_.id.equals(discount.incomeId))
        .map(_.occurrences.months.length)
        .map(multiplier => discount.amount * multiplier)
        .getOrElse(ZERO_REAIS)
    }).fold(ZERO_REAIS)((p, c) => (p+c).getOrElse(ZERO_REAIS))
  }

  private def getIncomesByContract(contract: Option[FinancialContract]) = {
    contract.map(c => incomeRepository.allByFinancialContractIds(c.id)).getOrElse(Future.successful(Seq()))
  }

  private def getDiscountsByIncomes(incomes: Seq[Income]) = {
    if (incomes.isEmpty)
      Future.successful(Seq())
    else
      incomeDiscountRepository.allByIncomeIds(incomes.map(_.id))
  }

  private def calculateIncomeGrossAmount(discounts: Seq[IncomeDiscount], income: Income) = {
    discounts
      .filter(_.incomeId.equals(income.id))
      .map(_.amount)
      .fold(ZERO_REAIS)((p, c) => (p + c).getOrElse(ZERO_REAIS))
      .sum(income.amount)
      .getOrElse(ZERO_REAIS)
  }
}

object DetailFinancialContractService {
  case class DetailedFinancialContract(
    id: String,
    name: String,
    contractType: ContractType.Value,
    companyCnpj: Option[String],
    incomes: Seq[DetailedIncome],
    totalYearlyGrossAmount: Amount,
    totalYearlyNetAmount: Amount,
    totalYearlyDiscountAmount: Amount,
    startDate: DateTime,
    endDate: Option[DateTime],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  case class DetailedIncome(
    id: String,
    name: String,
    grossAmount: Amount,
    netAmount: Amount,
    incomeType: IncomeType.Value,
    occurrences: Occurrences,
    discounts: Seq[DetailedIncomeDiscount],
    createdAt: DateTime,
    modifiedAt: DateTime,
  )

  case class DetailedIncomeDiscount(
    id: String,
    name: String,
    discountType: IncomeDiscountType.Value,
    amount: Amount,
    createdAt: DateTime,
    modifiedAt: DateTime,
  )
}
