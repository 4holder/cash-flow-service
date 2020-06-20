package income_management.repositories

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import domain.FinancialContract.ContractType
import domain._
import income_management.controllers.FinancialContractController.FinancialContractUpdateInput
import income_management.repositories.FinancialContractRepository.financialContracts
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FinancialContractRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                           (implicit ec: ExecutionContext)
  extends Repository {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  def allByUser(userId: String, page: Int, pageSize: Int): Future[Seq[FinancialContract]] = {
    val query =
      financialContracts
        .filter(r => r.user_id === userId)
        .sortBy(_.created_at.desc)
        .drop(offset(page, pageSize))
        .take(pageSize)

    db.run(query.result)
      .map(_.map(r => r: FinancialContract))
  }

  def getById(id: String): Future[Option[FinancialContract]] = {
    db.run(
      financialContracts
        .filter(r => r.id === id)
        .result
    ).map(r => r.headOption.map(fc => fc: FinancialContract))
  }

  def register(newFinancialContracts: FinancialContract*): Future[Unit] = {
    db.run(
      DBIO.seq(
        newFinancialContracts
          .map(newFC => financialContracts += newFC):_*
      )
    )
  }

  def update(id: String,
             financialContract: FinancialContractUpdateInput,
             now: DateTime = DateTime.now): Future[Int] = {
    db.run(
      financialContracts
        .filter(row => row.id === id)
        .map(fc => (
          fc.name,
          fc.company_cnpj,
          fc.contract_type,
          fc.start_date,
          fc.end_date,
          fc.modified_at,
        ))
        .update((
          financialContract.name,
          financialContract.companyCnpj,
          financialContract.contractType.toString,
          new Timestamp(financialContract.startDate.getMillis),
          financialContract.endDate.map(ed => new Timestamp(ed.getMillis)),
          new Timestamp(now.getMillis),
        ))
    )
  }

  def delete(id: String): Future[Int] = {
    db.run(
      financialContracts
        .filter(f => f.id === id)
        .delete
    )
  }

  def belongsToUser(id: String, user: User): Future[Boolean] = {
    db.run(
      financialContracts
        .filter(r => r.id === id && r.user_id === user.id)
        .length
        .result
    ).map(_ > 0)
  }
}

object FinancialContractRepository {
  val financialContracts = TableQuery[FinancialContractTable]

  case class FinancialContractDbRow(
    id: String,
    user_id: String,
    name: String,
    contract_type: String,
    company_cnpj: Option[String],
    start_date: Timestamp,
    end_date: Option[Timestamp],
    created_at: Timestamp,
    modified_at: Timestamp
  )

  object FinancialContractDbRow {
    implicit def toFinancialContract(financialContractDbRow: FinancialContractDbRow): FinancialContract = {
      FinancialContract(
        id = financialContractDbRow.id,
        user = User(financialContractDbRow.user_id),
        name = financialContractDbRow.name,
        contractType = ContractType.withName(financialContractDbRow.contract_type),
        companyCnpj = financialContractDbRow.company_cnpj,
        startDate = new DateTime(financialContractDbRow.start_date),
        endDate = financialContractDbRow.end_date.map(ed => new DateTime(ed)),
        createdAt = new DateTime(financialContractDbRow.created_at),
        modifiedAt = new DateTime(financialContractDbRow.modified_at)
      )
    }

    implicit def fromFinancialContract(financialContract: FinancialContract): FinancialContractDbRow = {
      FinancialContractDbRow(
        id = financialContract.id,
        user_id = financialContract.user.id,
        name = financialContract.name,
        contract_type = financialContract.contractType.toString,
        company_cnpj = financialContract.companyCnpj,
        start_date = new Timestamp(financialContract.startDate.getMillis),
        end_date = financialContract.endDate.map(d => new Timestamp(d.getMillis)),
        created_at = new Timestamp(financialContract.createdAt.getMillis),
        modified_at = new Timestamp(financialContract.modifiedAt.getMillis)
      )
    }
  }

  class FinancialContractTable(tag: Tag) extends Table[FinancialContractDbRow](tag, "financial_contracts") {
    def id = column[String]("id", O.PrimaryKey)
    def user_id = column[String]("user_id")
    def name = column[String]("name")
    def contract_type = column[String]("contract_type")
    def company_cnpj = column[Option[String]]("company_cnpj")
    def start_date = column[Timestamp]("start_date")
    def end_date = column[Option[Timestamp]]("end_date")
    def created_at = column[Timestamp]("created_at")
    def modified_at = column[Timestamp]("modified_at")

    def * = (
      id,
      user_id,
      name,
      contract_type,
      company_cnpj,
      start_date,
      end_date,
      created_at,
      modified_at
    ) <> ((FinancialContractDbRow.apply _).tupled, FinancialContractDbRow.unapply)
  }
}
