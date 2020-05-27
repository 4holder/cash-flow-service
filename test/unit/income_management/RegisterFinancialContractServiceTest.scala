package unit.income_management

import domain.financial_contract.FinancialContractRepository
import income_management.RegisterFinancialContractService
import org.joda.time.DateTime
import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import utils.builders.{FinancialContractBuilder, FinancialContractInputBuilder, UserBuilder}

import scala.concurrent.Future

class RegisterFinancialContractServiceTest extends AsyncFlatSpec with Matchers with MockitoSugar{
  private implicit val user = UserBuilder().build

  private val repository = mock[FinancialContractRepository]
  private val service = new RegisterFinancialContractService(repository)

  behavior of "register new financial contract"
  it should "register a valid financial contract" in {
    val now = DateTime.now
    val expectedFinancialContract = FinancialContractBuilder(
      user = user,
      createdAt = now,
      modifiedAt = now
    ).build

    val financialContractInput = FinancialContractInputBuilder(
      name = expectedFinancialContract.name,
      contractType = expectedFinancialContract.contractType,
      expectedFinancialContract.grossAmount,
      companyCnpj = expectedFinancialContract.companyCnpj,
      startDate = expectedFinancialContract.startDate,
      endDate = expectedFinancialContract.endDate
    ).build

    when(repository.insertFinancialContract(expectedFinancialContract))
        .thenReturn(Future.successful(expectedFinancialContract))

    service
      .register(financialContractInput, expectedFinancialContract.id, now)
      .map(financialContract => {
        financialContract shouldEqual expectedFinancialContract
      })
  }
}
