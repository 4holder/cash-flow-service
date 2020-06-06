package income_management

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import domain.Income.{IncomePayload, IncomeType}
import domain._
import income_management.FinancialContractRepository.financialContracts
import income_management.IncomeRepository.incomes
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{TableQuery, Tag}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext)
  extends Repository {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def allByFinancialContractId(financialContractId: String, page: Int, pageSize: Int): Future[Seq[Income]] = {
    val query =
      incomes
        .filter(_.financial_contract_id === financialContractId)
        .sortBy(_.created_at.desc)
        .drop(offset(page, pageSize))
        .take(pageSize)

    db.run(query.result)
      .map(_.map(r => r: Income))
  }

  def getById(id: String): Future[Option[Income]] = {
    db.run(
      incomes.filter(_.id === id).result
    ).map(r => r.headOption.map(income => income: Income))
  }

  def register(newIncomes: Income*): Future[Unit] = {
    db.run(
      DBIO.seq(
        newIncomes.map(income => incomes += income):_*
      )
    )
  }

  def update(id: String,
             incomePayload: IncomePayload,
             now: DateTime = DateTime.now): Future[Int] = {
    db.run(
      incomes
        .filter(_.id === id)
        .map(income => (
          income.name,
          income.income_type,
          income.value_in_cents,
          income.currency,
          income.occurrences,
          income.modified_at,
        )).update((
          incomePayload.name,
          incomePayload.incomeType.toString,
          incomePayload.amount.valueInCents,
          incomePayload.amount.currency,
          incomePayload.occurrences.toString,
          new Timestamp(now.getMillis),
        ))
    )
  }

  def delete(id: String): Future[Int] = {
    db.run(incomes.filter(_.id === id).delete)
  }

  def belongsToUser(id: String, user: User): Future[Boolean] = {
    db.run(
      incomes
        .join(financialContracts)
        .on(_.financial_contract_id === _.id)
        .filter(tuple => tuple._1.id === id && tuple._2.user_id === user.id)
        .length
        .result
    ).map(_ > 0)
  }
}

object IncomeRepository {
  val incomes = TableQuery[IncomeTable]

  case class IncomeDbRow(
    id: String,
    financial_contract_id: String,
    name: String,
    value_in_cents: Long,
    currency: String,
    income_type: String,
    occurrences: String,
    created_at: Timestamp,
    modified_at: Timestamp,
    is_active: Boolean = true,
  )

  object IncomeDbRow {
    implicit def toIncome(incomeDbRow: IncomeDbRow): Income = domain.Income(
      id = incomeDbRow.id,
      financialContractId = incomeDbRow.financial_contract_id,
      name = incomeDbRow.name,
      amount = Amount(
        valueInCents = incomeDbRow.value_in_cents,
        currency = Currency.withName(incomeDbRow.currency),
      ),
      incomeType = IncomeType.withName(incomeDbRow.income_type),
      occurrences = Occurrences(incomeDbRow.occurrences).get,
      createdAt = new DateTime(incomeDbRow.created_at),
      modifiedAt = new DateTime(incomeDbRow.modified_at),
    )

    implicit def fromIncome(income: Income): IncomeDbRow = IncomeDbRow(
      id = income.id,
      financial_contract_id = income.financialContractId,
      name = income.name,
      value_in_cents = income.amount.valueInCents,
      currency = income.amount.currency.toString,
      income_type = income.incomeType.toString,
      occurrences = income.occurrences.toString,
      created_at = new Timestamp(income.createdAt.getMillis),
      modified_at = new Timestamp(income.modifiedAt.getMillis),
    )
  }

  class IncomeTable(tag: Tag) extends Table[IncomeDbRow](tag, "incomes") {
    def id = column[String]("id", O.PrimaryKey)
    def financial_contract_id = column[String]("financial_contract_id")
    def name = column[String]("name")
    def value_in_cents = column[Long]("value_in_cents")
    def currency = column[String]("currency")
    def income_type = column[String]("income_type")
    def occurrences = column[String]("occurrences")
    def created_at = column[Timestamp]("created_at")
    def modified_at = column[Timestamp]("modified_at")
    def is_active = column[Boolean]("is_active")

    def * = (
      id,
      financial_contract_id,
      name,
      value_in_cents,
      currency,
      income_type,
      occurrences,
      created_at,
      modified_at,
      is_active,
    ) <> ((IncomeDbRow.apply _).tupled, IncomeDbRow.unapply)
  }
}
