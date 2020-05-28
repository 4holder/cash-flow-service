import clt_contract.{INSSTable, IRRFTable}
import domain.Currency.BRL
import domain.{Amount, AmountRange}

package object implicits {
  implicit val inssTable2020: INSSTable = clt_contract.INSSTable(
    List(
      AmountRange(
        from = Amount(0, BRL),
        to = domain.Amount(104500, BRL),
        percentageFactor = 0.075
      ),
      AmountRange(
        from = domain.Amount(104501, BRL),
        to = domain.Amount(208960, BRL),
        percentageFactor = 0.09
      ),
      AmountRange(
        from = domain.Amount(208961, BRL),
        to = domain.Amount(313440, BRL),
        percentageFactor = 0.12
      ),
      AmountRange(
        from = domain.Amount(313441, BRL),
        to = domain.Amount(610106, BRL),
        percentageFactor = 0.14
      )
    ),
    cap = domain.Amount(610106, BRL)
  )

  implicit val irrfTable2020: IRRFTable = clt_contract.IRRFTable(
    ranges = List(
      AmountRange(
        from = Amount(0, BRL),
        to = domain.Amount(190398, BRL),
        percentageFactor = 0
      ),
      AmountRange(
        from = domain.Amount(190399, BRL),
        to = domain.Amount(282665, BRL),
        percentageFactor = 0.075
      ),
      AmountRange(
        from = domain.Amount(282666, BRL),
        to = domain.Amount(375105, BRL),
        percentageFactor = 0.15
      ),
      AmountRange(
        from = domain.Amount(375106, BRL),
        to = domain.Amount(466468, BRL),
        percentageFactor = 0.225
      ),
      AmountRange(
        from = domain.Amount(466469, BRL),
        to = domain.Amount(Long.MaxValue, BRL),
        percentageFactor = 0.275
      )
    ),
    discountPerDependent = domain.Amount(18959, BRL)
  )
}
