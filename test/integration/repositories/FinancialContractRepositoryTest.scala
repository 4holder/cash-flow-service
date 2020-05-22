package integration.repositories

import income_management.models.financial_contract.{FinancialContract, FinancialContractRepository}
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import utils.{AsyncTest, DBUtils}
import utils.builders.FinancialContractBuilder

class FinancialContractRepositoryTest extends AsyncTest with BeforeAndAfter with BeforeAndAfterEach {
  private val firstFinancialContract = FinancialContractBuilder(
    name = "First Financial Contract",
    createdAt = DateTime.now.minusHours(1)
  ).build
  private val secondFinancialContract = FinancialContractBuilder(
    name = "Second Financial Contract",
    createdAt = DateTime.now.minusHours(2)
  ).build
  private val thirdFinancialContract = FinancialContractBuilder(
    name = "Third Financial Contract",
    createdAt = DateTime.now.minusHours(3)
  ).build
  private val forthFinancialContract = FinancialContractBuilder(
    name = "Forth Financial Contract",
    createdAt = DateTime.now.minusHours(4)
  ).build
  private val fifthFinancialContract = FinancialContractBuilder(
    name = "Fifth Financial Contract",
    createdAt = DateTime.now.minusHours(5)
  ).build

  private val repository = new FinancialContractRepository(dbConfig)

  after {
    DBUtils.clearDb()
  }

  override def beforeEach {
    DBUtils.clearDb()
  }

  behavior of "inserting financial contract"
  it should "insert a new financial contract in a empty db" in {
    val newFinancialContract = FinancialContractBuilder().build

    for {
      actualFinancialContract <- repository.insertContract(newFinancialContract)
      contracts <- DBUtils.allFinancialContractsRows
    } yield {
      val storedFinancialContract: FinancialContract = contracts.head

      storedFinancialContract shouldEqual newFinancialContract
      actualFinancialContract shouldEqual newFinancialContract
    }
  }

  it should "throw error when id is already used" in {
    val newFinancialContract = FinancialContractBuilder().build

    recoverToSucceededIf[PSQLException] {
      DBUtils
        .insertFinancialContracts(List(newFinancialContract))
        .flatMap(_ => repository.insertContract(newFinancialContract))
    }
  }

  behavior of "listing financial contracts"
  it should """return all financial contracts descending ordered by creation date
                when on first page with size 3 and there are 3 contracts""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        thirdFinancialContract,
        secondFinancialContract,
        firstFinancialContract
      ))
      financialContracts <- repository.getFinancialContracts(1, 3)
    } yield {
      financialContracts should have length 3

      financialContracts.head shouldEqual firstFinancialContract
      financialContracts(1) shouldEqual secondFinancialContract
      financialContracts.last shouldEqual thirdFinancialContract
    }
  }

  it should """return last 2 registered financial contracts
                when on first page with size 2 and there are 5 contracts""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        firstFinancialContract,
        secondFinancialContract,
        thirdFinancialContract,
        forthFinancialContract,
        fifthFinancialContract
      ))
      financialContracts <- repository.getFinancialContracts(1, 2)
    } yield {
      financialContracts should have length 2

      financialContracts.head shouldEqual firstFinancialContract
      financialContracts.last shouldEqual secondFinancialContract
    }
  }

  it should """return two financial contracts in the middle of set
                when on second page with size 2 and there are 5 contracts""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        firstFinancialContract,
        secondFinancialContract,
        thirdFinancialContract,
        forthFinancialContract,
        fifthFinancialContract
      ))
      financialContracts <- repository.getFinancialContracts(2, 2)
    } yield {
      financialContracts should have length 2

      financialContracts.head shouldEqual thirdFinancialContract
      financialContracts.last shouldEqual forthFinancialContract
    }
  }

  it should """return the last financial contract in the end of set
                when on third page with size 2 and there are 5 contracts""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        firstFinancialContract,
        secondFinancialContract,
        thirdFinancialContract,
        forthFinancialContract,
        fifthFinancialContract
      ))
      financialContracts <- repository.getFinancialContracts(3, 2)
    } yield {
      financialContracts should have length 1

      financialContracts.head shouldEqual fifthFinancialContract
    }
  }
}
