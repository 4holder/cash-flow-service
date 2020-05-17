package domain

case class IRRFTable(
  ranges: List[AmountRange],
  discountPerDependent: Amount
)
