package unit.income_management

import income_management.DetailFinancialContractService
import income_management.repositories.{FinancialContractRepository, IncomeDiscountRepository, IncomeRepository}
import utils.AsyncUnitSpec

class DetailFinancialContractServiceTest extends AsyncUnitSpec {
  private val financialContractRepository = mock[FinancialContractRepository]
  private val incomeRepository = mock[IncomeRepository]
  private val incomeDiscountRepository = mock[IncomeDiscountRepository]
  private val service = new DetailFinancialContractService(
    financialContractRepository,
    incomeRepository,
    incomeDiscountRepository
  )


}
