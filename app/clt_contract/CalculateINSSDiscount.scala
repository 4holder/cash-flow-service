package clt_contract

import domain.Amount
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}

import scala.util.{Failure, Success, Try}

object CalculateINSSDiscount {
  def apply(grossSalary: Amount)
           (implicit inssTable: INSSTable): Try[IncomeDiscountPayload] = {
    if (grossSalary.valueInCents <= 0) {
      return Try(IncomeDiscountPayload(
        "INSS",
        IncomeDiscountType.INSS.toString,
        Amount.ZERO_REAIS,
        0.0
      ))
    }

    val discountsTryable = findDiscounts(grossSalary)

    discountsTryable
      .map(discounts => {
        val discountInCents = discounts.map(_.valueInCents).sum
        val percentage = grossSalary.percentsFor(discountInCents)

        IncomeDiscountPayload(
          "INSS",
          IncomeDiscountType.INSS.toString,
          Amount(discountInCents, grossSalary.currency),
          percentage
        )
      })
  }

  private def findDiscounts(salaryAmount: Amount,
                            discountValues: List[Amount] = List())
                           (implicit inssTable: INSSTable): Try[List[Amount]] = {


    if(discountValues.isEmpty) {
      val firstRange = inssTable.ranges.head
      val firstDiscount = firstRange.to % firstRange.percentageFactor

      return (salaryAmount - firstRange.to)
        .flatMap(d => findDiscounts(d, List(firstDiscount)))
    }

    if (discountValues.size >= inssTable.ranges.size) {
      return Success(discountValues)
    }

    val previousRange = inssTable.ranges(discountValues.size - 1)
    val currentRange = inssTable.ranges(discountValues.size)

    val baseRangeTryable = currentRange.to - previousRange.to

    baseRangeTryable
      .flatMap(_.isGreaterThan(salaryAmount)) match {
      case Success(true) =>
        val currentDiscount = salaryAmount % currentRange.percentageFactor
        Success(discountValues :+ currentDiscount)
      case Failure(e) => Failure(e)
      case _ =>
        for {
          baseRange <- baseRangeTryable
          nextRange <- salaryAmount - baseRange
          discount <- baseRangeTryable.map(_ % currentRange.percentageFactor)
          discountFounds <- findDiscounts(nextRange, discountValues :+ discount)
        } yield discountFounds
    }
  }
}
