package income_management

import java.util.UUID.randomUUID

import com.google.inject.{Inject, Singleton}
import domain.Income
import domain.Income.{IncomePayload, IncomeType}
import income_management.repositories.IncomeRepository
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterIncomeService @Inject()(incomeRepository: IncomeRepository)
                                     (implicit ec: ExecutionContext) {

  def register(
    financialContractId: String,
    incomePayload: IncomePayload,
    incomeId: String = randomUUID().toString,
    now: DateTime = DateTime.now(),
  ): Future[Income] = {
    val newIncome = Income(
      id = incomeId,
      financialContractId = financialContractId,
      name = incomePayload.name,
      amount = incomePayload.amount,
      incomeType = IncomeType.withName(incomePayload.incomeType),
      occurrences = incomePayload.occurrences,
      createdAt = now,
      modifiedAt = now,
    )

    incomeRepository.register(newIncome).map(_ => newIncome)
  }

}
