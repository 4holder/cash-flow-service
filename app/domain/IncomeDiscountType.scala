package domain

case object IncomeDiscountType extends Enumeration {
  val INSS: IncomeDiscountType.Value = Value("INSS")
  val IRRF: IncomeDiscountType.Value = Value("IRRF")
}
