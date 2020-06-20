package utils.builders

import domain.Amount
import domain.FinancialContract.ContractType
import domain.Income.IncomeType
import income_management.controllers.FinancialContractController.{FinancialContractRegisterInput, IncomeRegisterInput}
import org.joda.time.DateTime

case class FinancialContractRegisterInputBuilder(
  name: String = "A Good Contract Payload",
  contractType: ContractType.Value = ContractType.CLT,
  grossAmount: Amount = Amount.BRL(8935031),
  companyCnpj: Option[String] = Some("93113309001234"),
  startDate: DateTime = DateTime.now.minusYears(7),
  endDate: Option[DateTime] = Some(DateTime.now),
  incomes: List[IncomeRegisterInput] = List(
    IncomeRegisterInputBuilder(
      amount = Amount.BRL(300000),
      incomeType = IncomeType.SALARY
    ).build,
    IncomeRegisterInputBuilder(
      amount = Amount.BRL(150000),
      incomeType = IncomeType.THIRTEENTH_SALARY_ADVANCE,
      discounts = List(),
    ).build,
    IncomeRegisterInputBuilder(
      amount = Amount.BRL(150000),
      incomeType = IncomeType.THIRTEENTH_SALARY,
    ).build,
  )
) {
  def build: FinancialContractRegisterInput = {
    FinancialContractRegisterInput(
      name,
      contractType.toString,
      companyCnpj,
      startDate,
      endDate,
      incomes,
    )
  }
}