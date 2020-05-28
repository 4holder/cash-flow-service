package clt_contract

import domain.Amount
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}

import scala.util.{Failure, Success, Try}

object CalculateIRRFDiscount {
  def apply(grossSalary: Amount,
            inssDiscount: IncomeDiscountPayload,
            dependentQuantity: Int,
            otherDeductions: Amount)
           (implicit irrfTable: IRRFTable): Try[IncomeDiscountPayload] = {
    val dependentsDeduction = irrfTable.discountPerDependent.multiply(dependentQuantity)
    val baseSalary = grossSalary.subtract(inssDiscount.amount, otherDeductions, dependentsDeduction)

    baseSalary
      .flatMap(s => findDiscounts(s))
      .map(discounts => {
        val discountInCents = discounts.map(_.valueInCents).sum
        val percentage = grossSalary.percentsFor(discountInCents)

        IncomeDiscountPayload(
          "IRRF",
          IncomeDiscountType.IRRF.toString,
          Amount(discountInCents, grossSalary.currency),
          percentage
        )
      })
  }

  private def findDiscounts(baseSalary: Amount,
                            discountValues: List[Amount] = List())
                           (implicit irrfTable: IRRFTable): Try[List[Amount]] = {
    if (baseSalary.isNegative) {
      return Success(discountValues)
    }

    if (discountValues.isEmpty) {
      val firstRange = irrfTable.ranges.head

      return baseSalary
              .subtract(firstRange.to)
              .flatMap(d => findDiscounts(d, List(Amount.ZERO_REAIS)))
    }

    if (discountValues.size >= irrfTable.ranges.size) {
      return Success(discountValues)
    }

    val previousRange = irrfTable.ranges(discountValues.size - 1)
    val currentRange = irrfTable.ranges(discountValues.size)

    val baseRangeTryable = currentRange.to - previousRange.to

    baseRangeTryable
      .flatMap(_.isGreaterThan(baseSalary)) match {
      case Success(true) =>
        val currentDiscount = baseSalary % currentRange.percentageFactor
        Success(discountValues :+ currentDiscount)
      case Failure(e) => Failure(e)
      case _ =>
        for {
          baseRange <- baseRangeTryable
          nextRange <- baseSalary - baseRange
          discount <- baseRangeTryable.map(_ % currentRange.percentageFactor)
          discountFounds <- findDiscounts(nextRange, discountValues :+ discount)
        } yield discountFounds
    }
  }
}
