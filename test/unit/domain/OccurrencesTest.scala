package unit.domain

import domain.Occurrences
import org.scalatest.{FlatSpec, Matchers}
import domain.exceptions.InvalidCronExpressionException

class OccurrencesTest extends FlatSpec with Matchers {
  val allMonths = List(1,2,3,4,5,6,7,8,9,10,11,12)

  behavior of "parse from string"
  it should "properly parse an every november 25th occurrences" in {
    val cronExpr = "25 11"

    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isSuccess shouldEqual true
    val actualOccurrence = occurrenceTryable.get

    actualOccurrence.day shouldEqual 25
    actualOccurrence.months shouldEqual List(11)
  }

  it should "properly parse an every month at 5th occurrences" in {

    val cronExpr = s"5 ${allMonths.mkString(",")}"
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isSuccess shouldEqual true
    val actualOccurrence = occurrenceTryable.get

    actualOccurrence.day shouldEqual 5
    actualOccurrence.months shouldEqual allMonths
  }

  it should "properly parse an every month at 5th occurrences using wild card" in {
    val cronExpr = s"5 *"
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isSuccess shouldEqual true
    val actualOccurrence = occurrenceTryable.get

    actualOccurrence.day shouldEqual 5
    actualOccurrence.months shouldEqual allMonths
  }

  behavior of "parsing invalid string"
  it should "throw invalid expression when no month is provided" in {
    val cronExpr = s"5 "
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isFailure shouldEqual true
    occurrenceTryable.failed.get shouldBe an[InvalidCronExpressionException]
  }

  it should "throw invalid expression when alphabetical chars are provided" in {
    val cronExpr = s"c A,B,2"
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isFailure shouldEqual true
    occurrenceTryable.failed.get shouldBe an[InvalidCronExpressionException]
  }

  it should "throw invalid expression when alphabetical chars along with range are provided" in {
    val cronExpr = s"c 1-2"
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isFailure shouldEqual true
    occurrenceTryable.failed.get shouldBe an[InvalidCronExpressionException]
  }

  it should "throw invalid expression when a month range is provided" in {
    val cronExpr = s"1 1-3"
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isFailure shouldEqual true
    occurrenceTryable.failed.get shouldBe an[InvalidCronExpressionException]
  }

  it should "throw invalid expression when a day range is provided" in {
    val cronExpr = s"1-2 4,5,6"
    val occurrenceTryable = Occurrences(cronExpr)

    occurrenceTryable.isFailure shouldEqual true
    occurrenceTryable.failed.get shouldBe an[InvalidCronExpressionException]
  }
}
