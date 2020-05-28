package clt_contract

import domain.{Amount, AmountRange}

case class INSSTable (
  ranges: List[AmountRange],
  cap: Amount
)
