package clt_contract

import domain.{Amount, AmountRange}

case class IRRFTable(
  ranges: List[AmountRange],
  discountPerDependent: Amount
)
