package income_management

import java.util.UUID.randomUUID

import com.google.inject.{Inject, Singleton}
import domain.FinancialContract.ContractType
import domain.{FinancialContract, Income, IncomeDiscount, User}
import income_management.FinancialContractController.FinancialContractRegisterInput
import income_management.repositories.FinancialContractRepository
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterFinancialContractService @Inject()(
  repository: FinancialContractRepository,
  registerIncomeService: RegisterIncomeService,
)
(implicit ec: ExecutionContext){
  def register(newFinancialContractInput: FinancialContractRegisterInput)
              (implicit user: User, now: DateTime = DateTime.now): Future[(FinancialContract, Seq[(Income, Seq[IncomeDiscount])])] = {
    val financialContractId = randomUUID().toString
    val financialContract = FinancialContract(
      id = financialContractId,
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

    for {
      _ <- repository.register(financialContract)
      incomesAndDiscounts <-
        registerIncomeService
          .addIncomesToFinancialContract(financialContractId, newFinancialContractInput.incomes:_*)
    } yield (financialContract, incomesAndDiscounts)
  }
}
