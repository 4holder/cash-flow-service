package income_management

import java.util.UUID.randomUUID

import com.google.inject.{Inject, Singleton}
import domain.IncomeDiscount
import domain.IncomeDiscount.IncomeDiscountType
import income_management.controllers.FinancialContractController.IncomeRegisterDiscountInput
import income_management.repositories.IncomeDiscountRepository
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegisterIncomeDiscountService @Inject()(incomeDiscountRepository: IncomeDiscountRepository)
                                             (implicit ec: ExecutionContext) {

  def addDiscountToIncome(
    incomeId: String,
    incomeDiscountInputs: IncomeRegisterDiscountInput*,
  )(implicit now: DateTime = DateTime.now()): Future[Seq[IncomeDiscount]] = {
    val newIncomeDiscounts = incomeDiscountInputs.map(discountInput => IncomeDiscount(
      id = randomUUID().toString,
      incomeId = incomeId,
      name = discountInput.name,
      amount = discountInput.amount,
      discountType = IncomeDiscountType.withName(discountInput.discountType),
      createdAt = now,
      modifiedAt = now,
    ))

    incomeDiscountRepository
      .register(newIncomeDiscounts:_*)
      .map(_ => newIncomeDiscounts)
  }
}
