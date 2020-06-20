package unit.income_management

import domain.{FinancialContract, User}
import income_management.controllers.FinancialContractController.IncomeRegisterInput
import income_management.repositories.FinancialContractRepository
import income_management.{RegisterFinancialContractService, RegisterIncomeService}
import org.joda.time.DateTime
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import utils.builders.{FinancialContractRegisterInputBuilder, IncomeBuilder, IncomeDiscountBuilder, UserBuilder}

import scala.concurrent.Future

class RegisterFinancialContractServiceTest extends AsyncFlatSpec with Matchers with MockitoSugar{
  private val user: User = UserBuilder().build
  private val now = DateTime.now

  private val repository = mock[FinancialContractRepository]
  private val registerIncomeService = mock[RegisterIncomeService]
  private val registerFinancialContractService =
    new RegisterFinancialContractService(repository, registerIncomeService)

  behavior of "register new financial contract"
  it should "register a valid financial contract" in {
    val financialContractInput = FinancialContractRegisterInputBuilder().build
    val income = IncomeBuilder().build
    val incomeDiscount = IncomeDiscountBuilder().build
    val expectedIncomeAndDiscounts = Seq((income, Seq(incomeDiscount)))

    when(repository.register(any[FinancialContract]))
        .thenReturn(Future{})
    when(registerIncomeService.addIncomesToFinancialContract(
      any[String],
      any[IncomeRegisterInput],
    )(eqTo(now)),
    ).thenReturn(Future.successful(expectedIncomeAndDiscounts))

    registerFinancialContractService
      .register(financialContractInput)(user, now)
      .map(financialContract => {
        financialContract._1.name shouldEqual financialContractInput.name
        financialContract._1.user.id shouldEqual user.id
        financialContract._1.contractType.toString shouldEqual financialContractInput.contractType.toString
        financialContract._1.companyCnpj shouldEqual financialContractInput.companyCnpj
        financialContract._1.startDate shouldEqual financialContractInput.startDate
        financialContract._1.endDate shouldEqual financialContractInput.endDate

        financialContract._2 shouldEqual expectedIncomeAndDiscounts
      })
  }
}
