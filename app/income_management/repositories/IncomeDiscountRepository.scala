package income_management.repositories

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import domain.IncomeDiscount.{IncomeDiscountPayload, IncomeDiscountType}
import domain._
import income_management.repositories.IncomeDiscountRepository.IncomeDiscountTable
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{TableQuery, Tag}
import FinancialContractRepository.financialContracts
import IncomeRepository.incomes
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeDiscountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider,
                                         incomeRepository: IncomeRepository)
                                        (implicit ec: ExecutionContext)
  extends Repository {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val incomeDiscountTable = TableQuery[IncomeDiscountTable]

  import dbConfig._
  import profile.api._

  def allByIncomeIds(incomeIds: Seq[String]): Future[Seq[IncomeDiscount]] = {
    val query = incomeDiscountTable
      .filter(_.income_id inSet incomeIds)
      .sortBy(_.created_at.desc)

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

  def update(id: String,
             incomeDiscountPayload: IncomeDiscountPayload,
             now: DateTime = DateTime.now): Future[Int] = {
    db.run(
      incomeDiscountTable
        .filter(_.id === id)
        .map(incomeDiscount => (
          incomeDiscount.name,
          incomeDiscount.discount_type,
          incomeDiscount.value_in_cents,
          incomeDiscount.currency,
          incomeDiscount.modified_at,
        )).update((
        incomeDiscountPayload.name,
        incomeDiscountPayload.discountType.toString,
        incomeDiscountPayload.amount.valueInCents,
        incomeDiscountPayload.amount.currency,
        new Timestamp(now.getMillis),
      ))
    )
  }

  def delete(id: String): Future[Int] = {
    db.run(incomeDiscountTable.filter(_.id === id).delete)
  }

  def belongsToUser(id: String, user: User): Future[Boolean] = {
    db.run(
      incomeDiscountTable
        .join(incomes)
        .on(_.income_id === _.id)
        .join(financialContracts)
        .on(_._2.financial_contract_id === _.id)
        .filter(tuple => tuple._1._1.id === id && tuple._2.user_id === user.id)
        .length
        .result
    ).map(_ > 0)
  }

  override def parentBelongsToUser(parentId: String, user: User): Future[Boolean] = {
    incomeRepository.belongsToUser(parentId, user)
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
    def created_at = column[Timestamp]("created_at")
    def modified_at = column[Timestamp]("modified_at")

    def * = (
      id,
      income_id,
      name,
      value_in_cents,
      currency,
      discount_type,
      created_at,
      modified_at,
    ) <> ((IncomeDiscountDbRow.apply _).tupled, IncomeDiscountDbRow.unapply)
  }
}