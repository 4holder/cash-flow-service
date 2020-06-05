package income_management

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import domain.IncomeDiscount.IncomeDiscountType
import domain.{Amount, Currency, IncomeDiscount, Repository}
import income_management.IncomeDiscountRepository.IncomeDiscountTable
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeDiscountRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                        (implicit ec: ExecutionContext)
  extends Repository {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val incomeDiscountTable = TableQuery[IncomeDiscountTable]

  import dbConfig._
  import profile.api._

  def allByIncomeId(incomeId: String, page: Int, pageSize: Int): Future[Seq[IncomeDiscount]] = {
    val query = incomeDiscountTable
      .filter(_.income_id === incomeId)
      .sortBy(_.created_at.desc)
      .drop(offset(page, pageSize))
      .take(pageSize)

    db.run(query.result)
      .map(_.map(r => r: IncomeDiscount))
  }

  def getById(id: String): Future[Option[IncomeDiscount]] = {
    db.run(
      incomeDiscountTable.filter(_.id === id).result
    ).map(r => r.headOption.map(incomeDiscount => incomeDiscount: IncomeDiscount))
  }

  def register(newIncomeDiscounts: IncomeDiscount*): Future[Unit] = {
    db.run(
      DBIO.seq(
        newIncomeDiscounts.map(incomeDiscount => incomeDiscountTable += incomeDiscount):_*
      )
    )
  }
}

object IncomeDiscountRepository {
  case class IncomeDiscountDbRow(
    id: String,
    income_id: String,
    name: String,
    value_in_cents: Long,
    currency: String,
    discount_type: String,
    aliquot: Double,
    created_at: Timestamp,
    modified_at: Timestamp,
  )

  object IncomeDiscountDbRow {
    implicit def fromIncomeDiscount(incomeDiscount: IncomeDiscount): IncomeDiscountDbRow = {
      IncomeDiscountDbRow(
        id = incomeDiscount.id,
        income_id = incomeDiscount.incomeId,
        name = incomeDiscount.name,
        value_in_cents = incomeDiscount.amount.valueInCents,
        currency = incomeDiscount.amount.currency.toString,
        discount_type = incomeDiscount.discountType.toString,
        aliquot = incomeDiscount.aliquot,
        created_at = new Timestamp(incomeDiscount.createdAt.getMillis),
        modified_at = new Timestamp(incomeDiscount.modifiedAt.getMillis),
      )
    }

    implicit def toIncomeDiscount(incomeDiscountDbRow: IncomeDiscountDbRow): IncomeDiscount = {
      IncomeDiscount(
        id = incomeDiscountDbRow.id,
        incomeId = incomeDiscountDbRow.income_id,
        name = incomeDiscountDbRow.name,
        discountType = IncomeDiscountType.withName(incomeDiscountDbRow.discount_type),
        amount = Amount(
          incomeDiscountDbRow.value_in_cents,
          Currency.withName(incomeDiscountDbRow.currency)
        ),
        aliquot = incomeDiscountDbRow.aliquot,
        createdAt = new DateTime(incomeDiscountDbRow.created_at),
        modifiedAt = new DateTime(incomeDiscountDbRow.modified_at)
      )
    }
  }

  class IncomeDiscountTable(tag: Tag) extends Table[IncomeDiscountDbRow](tag, "income_discounts") {
    def id = column[String]("id", O.PrimaryKey)
    def income_id = column[String]("income_id")
    def name = column[String]("name")
    def value_in_cents = column[Long]("value_in_cents")
    def currency = column[String]("currency")
    def discount_type = column[String]("discount_type")
    def aliquot = column[Double]("aliquot")
    def created_at = column[Timestamp]("created_at")
    def modified_at = column[Timestamp]("modified_at")

    def * = (
      id,
      income_id,
      name,
      value_in_cents,
      currency,
      discount_type,
      aliquot,
      created_at,
      modified_at,
    ) <> ((IncomeDiscountDbRow.apply _).tupled, IncomeDiscountDbRow.unapply)
  }
}