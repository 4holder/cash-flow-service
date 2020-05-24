package income_management

import java.util.UUID.randomUUID

import com.google.inject.{Inject, Singleton}
import domain.financial_contract.FinancialContract.FinancialContractPayload
import domain.financial_contract.{FinancialContract, FinancialContractRepository}
import domain.{ContractType, User}
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterFinancialContractService @Inject()(repository: FinancialContractRepository)
                                                (implicit ec: ExecutionContext){
  def register(
    newFinancialContractInput: FinancialContractPayload,
    id: String = randomUUID().toString,
    now: DateTime = DateTime.now
  )(implicit user: User): Future[FinancialContract] = {
    val financialContract = FinancialContract(
      id = id,
      name = newFinancialContractInput.name,
      user = User(user.id),
      contractType = ContractType.withName(newFinancialContractInput.contractType),
      grossAmount = newFinancialContractInput.grossAmount,
      companyCnpj = newFinancialContractInput.companyCnpj,
      startDate = newFinancialContractInput.startDate,
      endDate = newFinancialContractInput.endDate,
      createdAt = now,
      modifiedAt = now
    )

    repository
      .insertContract(financialContract)
  }
}
