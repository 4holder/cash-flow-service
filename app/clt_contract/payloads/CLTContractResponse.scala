package clt_contract.payloads

import clt_contract.CLTContract
import domain.{Amount, Income, IncomeDiscount, Occurrences}
import play.api.libs.json.{Json, Writes}

case class AmountPayload(
  valueInCents: Long,
  currency: String
)

object AmountPayload {
  implicit def fromAmount(amount: Amount): AmountPayload = {
    AmountPayload(
      valueInCents = amount.valueInCents,
      currency = amount.currency.toString
    )
  }
}

case class OccurrencesPayload(
  day: Int,
  months: List[Int]
)

object OccurrencesPayload {
  implicit def fromOccurrences(occurrences: Occurrences): OccurrencesPayload = {
    OccurrencesPayload(
      day = occurrences.day,
      months = occurrences.months
    )
  }
}

case class IncomeDiscountPayload(
  name: String,
  discountType: String,
  amount: AmountPayload,
  grossAmountAliquot: Double
)

object IncomeDiscountPayload {
  implicit def fromIncomeDiscount(discount: IncomeDiscount): IncomeDiscountPayload = {
    IncomeDiscountPayload(
      name = discount.name,
      discountType = discount.discountType.toString,
      amount = discount.amount,
      grossAmountAliquot = discount.grossAmountAliquot
    )
  }
}

case class IncomePayload(
  name: String,
  amount: AmountPayload,
  incomeType: String,
  occurrences: OccurrencesPayload,
  discounts: List[IncomeDiscountPayload]
)

object IncomePayload {
  implicit def fromIncome(income: Income): IncomePayload = {
    IncomePayload(
      name = income.name,
      amount = income.amount,
      incomeType = income.incomeType.toString,
      occurrences = income.occurrences,
      discounts = income.discounts.map(discount => discount: IncomeDiscountPayload)
    )
  }
}

case class CLTContractResponse(
  grossSalary: AmountPayload,
  incomes: List[IncomePayload]
)

object CLTContractResponse {
  implicit val amountPayload: Writes[AmountPayload] = Json.writes[AmountPayload]
  implicit val occurrencesPayload: Writes[OccurrencesPayload] = Json.writes[OccurrencesPayload]
  implicit val incomeDiscountPayloadWrites: Writes[IncomeDiscountPayload] = Json.writes[IncomeDiscountPayload]
  implicit val incomePayloadWrites: Writes[IncomePayload] = Json.writes[IncomePayload]
  implicit val cltContractResponseWrites: Writes[CLTContractResponse] = Json.writes[CLTContractResponse]

  implicit def fromCLTContract(cltContract: CLTContract): CLTContractResponse = {
    CLTContractResponse(
      grossSalary = cltContract.grossSalary,
      incomes = cltContract.incomes.map(income => income: IncomePayload)
    )
  }
}