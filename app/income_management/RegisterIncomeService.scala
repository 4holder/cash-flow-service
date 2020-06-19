package income_management

import java.util.UUID.randomUUID

import com.google.inject.{Inject, Singleton}
import domain.{Income, IncomeDiscount}
import domain.Income.IncomeType
import income_management.FinancialContractController.IncomeRegisterInput
import income_management.repositories.IncomeRepository
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterIncomeService @Inject()(incomeRepository: IncomeRepository,
                                      registerIncomeDiscountService: RegisterIncomeDiscountService)
                                     (implicit ec: ExecutionContext) {

  def addIncomesToFinancialContract(
    financialContractId: String,
    incomeInputs: IncomeRegisterInput*,
  )(implicit now: DateTime = DateTime.now()): Future[Seq[(Income, Seq[IncomeDiscount])]] = {
    val newIncomeMap = incomeInputs.map(incomeInput => (
      Income(
        id = randomUUID().toString,
        financialContractId = financialContractId,
        name = incomeInput.name,
        amount = incomeInput.amount,
        incomeType = IncomeType.withName(incomeInput.incomeType),
        occurrences = incomeInput.occurrences,
        createdAt = now,
        modifiedAt = now,
      ), incomeInput.discounts)
    )

    for {
      _ <- incomeRepository.register(newIncomeMap.map(_._1):_*)
      discounts <- Future.sequence(newIncomeMap.map(tuple =>
        registerIncomeDiscountService.addDiscountToIncome(tuple._1.id, tuple._2:_*)))
    } yield newIncomeMap.map(tuple => (tuple._1, discounts.flatten.filter(_.id.equals(tuple._1.id))))
  }
}
