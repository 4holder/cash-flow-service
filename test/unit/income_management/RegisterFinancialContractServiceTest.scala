package unit.income_management

import domain.{FinancialContract, User}
import income_management.FinancialContractController.IncomeRegisterInput
import income_management.{RegisterFinancialContractService, RegisterIncomeService}
import income_management.repositories.FinancialContractRepository
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import utils.builders.{FinancialContractBuilder, FinancialContractRegisterInputBuilder, UserBuilder}
import org.mockito.Matchers.{any, eq => eqTo}

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

    when(repository.register(any[FinancialContract]))
        .thenReturn(Future{})
    when(registerIncomeService.addIncomesToFinancialContract(
      any[String],
      any[IncomeRegisterInput],
    )(eqTo(now)),
    ).thenReturn(Future{null})

    registerFinancialContractService
      .register(financialContractInput)(user, now)
      .map(financialContract => {
        financialContract._1.name shouldEqual financialContractInput.name
      })
  }
}
