package utils.builders

import java.util.UUID.randomUUID

import domain.Amount
import income_management.ResumeFinancialContractsService.FinancialContractResume
import org.joda.time.DateTime

case class FinancialContractResumeBuilder(
  id: String = randomUUID().toString,
  name: String = s"Contract X - ${DateTime.now}",
  yearlyGrossIncome: Option[Amount] = Some(Amount.BRL(1203912)),
  yearlyNetIncome: Option[Amount] = Some(Amount.BRL(1000000)),
  yearlyIncomeDiscount: Option[Amount] = Some(Amount.BRL(203912))
) {
  def build: FinancialContractResume = FinancialContractResume(
    id = id,
    name = name,
    yearlyGrossIncome = yearlyGrossIncome,
    yearlyNetIncome = yearlyNetIncome,
    yearlyIncomeDiscount = yearlyIncomeDiscount,
  )
}
