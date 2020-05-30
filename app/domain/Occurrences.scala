package domain

import domain.Occurrences.AbstractOccurrences
import domain.exceptions.InvalidCronExpressionException
import play.api.libs.json.{Json, Reads, Writes}

import scala.util.{Failure, Success, Try}

case class Occurrences(
  day: Int,
  months: List[Int]
) extends AbstractOccurrences

object Occurrences {
  implicit val occurrencesWrites: Writes[Occurrences] = Json.writes[Occurrences]
  private val ALL_MONTHS = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
  private val CRON_SEPARATOR = " "
  private val MONTH_SEPARATOR = ","

  trait AbstractOccurrences {
    val day: Int
    val months: List[Int]

    override def equals(obj: Any): Boolean = {
      obj match {
        case payload: OccurrencesPayload  =>
          this.day.equals(payload.day) &&
            this.months.equals(payload.months)
        case occurrences: Occurrences  =>
          this.day.equals(occurrences.day) &&
            this.months.equals(occurrences.months)
        case _ => super.equals(obj)
      }
    }

    override def toString: String = {
      s"$day $monthsToString"
    }

    private def monthsToString = {
      if(months == Occurrences.ALL_MONTHS) {
        "*"
      } else {
        months.mkString(Occurrences.MONTH_SEPARATOR)
      }
    }
  }

  def apply(cronExpression: String): Try[Occurrences] = {
    validateExpression(cronExpression)
      .map { expr =>
        val dayAndMonths = expr.split(CRON_SEPARATOR)

        val day = dayAndMonths(0).toInt
        val monthsExpr = dayAndMonths(1)

        val months = monthsExpr match {
          case "*" => ALL_MONTHS
          case _ =>
            monthsExpr.split(MONTH_SEPARATOR).map(_.trim.toInt).toList
        }

        Occurrences(day, months)
      }
  }

  private def validateExpression(cronExpression: String) : Try[String] = {
    val regularExpr = """((\d+,)+\d+|(\d+)) ((\d+,)+\d+|(\d+)|\d+|\*)""".r

    if (regularExpr.pattern.matcher(cronExpression).matches()) {
      Success(cronExpression)
    } else {
      Failure(InvalidCronExpressionException(s"Invalid expression '$cronExpression'"))
    }
  }

  class OccurrencesBuilder {
    private var day: Int = _
    private var months: List[Int] = _

    def day(d: Int): OccurrencesBuilder = {
      this.day = d
      this
    }

    def months(months: List[Int]): OccurrencesBuilder = {
      this.months = months
      this
    }

    def month(month: Int): OccurrencesBuilder = {
      this.months = List(month)
      this
    }

    def allMonths: OccurrencesBuilder = {
      this.months = ALL_MONTHS
      this
    }

    def build: Occurrences = {
      Occurrences(day, months)
    }
  }

  def builder = new OccurrencesBuilder

  case class OccurrencesPayload(
    day: Int,
    months: List[Int]
  ) extends AbstractOccurrences

  object OccurrencesPayload {
    implicit val occurrencesPayloadWrites: Writes[OccurrencesPayload] = Json.writes[OccurrencesPayload]
    implicit val occurrencesPayloadReads: Reads[OccurrencesPayload] = Json.reads[OccurrencesPayload]

    implicit def fromOccurrences(occurrences: Occurrences): OccurrencesPayload = {
      OccurrencesPayload(
        day = occurrences.day,
        months = occurrences.months
      )
    }

    implicit def toOccurrences(occurrences: OccurrencesPayload): Occurrences = {
      Occurrences(
        day = occurrences.day,
        months = occurrences.months
      )
    }
  }
}
