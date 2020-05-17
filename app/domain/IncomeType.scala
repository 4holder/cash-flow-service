package domain

case object IncomeType extends Enumeration {
  val SALARY: IncomeType.Value = Value("SALARY")
  val THIRTEENTH_SALARY: IncomeType.Value = Value("THIRTEENTH_SALARY")
  val THIRTEENTH_SALARY_ADVANCE: IncomeType.Value = Value("THIRTEENTH_SALARY_ADVANCE")
  val PROFIT_SHARING: IncomeType.Value = Value("PROFIT_SHARING")
}
