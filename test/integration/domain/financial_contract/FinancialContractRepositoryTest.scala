package integration.domain.financial_contract

import domain.User
import domain.financial_contract.{FinancialContract, FinancialContractRepository}
import org.joda.time.DateTime
import org.postgresql.util.PSQLException
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import utils.builders.{FinancialContractBuilder, UserBuilder}
import utils.{AsyncTest, DBUtils}

class FinancialContractRepositoryTest extends AsyncTest with BeforeAndAfter with BeforeAndAfterEach {
  implicit private val user: User = UserBuilder().build

  private val firstFinancialContract = FinancialContractBuilder(
    user = user,
    name = "First Financial Contract",
    createdAt = DateTime.now.minusHours(1)
  ).build
  private val secondFinancialContract = FinancialContractBuilder(
    user = user,
    name = "Second Financial Contract",
    createdAt = DateTime.now.minusHours(2)
  ).build
  private val thirdFinancialContract = FinancialContractBuilder(
    user = user,
    name = "Third Financial Contract",
    createdAt = DateTime.now.minusHours(3)
  ).build
  private val forthFinancialContract = FinancialContractBuilder(
    user = user,
    name = "Forth Financial Contract",
    createdAt = DateTime.now.minusHours(4)
  ).build
  private val fifthFinancialContract = FinancialContractBuilder(
    user = user,
    name = "Fifth Financial Contract",
    createdAt = DateTime.now.minusHours(5)
  ).build
  private val firstNoiseFinancialContract = FinancialContractBuilder(createdAt = DateTime.now.minusMinutes(30)).build
  private val secondNoiseFinancialContract = FinancialContractBuilder().build

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

  behavior of "listing user financial contracts"
  it should """return all financial contracts descending ordered by creation date
                when on first page with size 3 and there are 3 contracts""" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        thirdFinancialContract,
        firstNoiseFinancialContract,
        secondFinancialContract,
        secondNoiseFinancialContract,
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
        secondFinancialContract,
        forthFinancialContract,
        firstNoiseFinancialContract,
        firstFinancialContract,
        secondNoiseFinancialContract,
        thirdFinancialContract,
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
        firstNoiseFinancialContract,
        fifthFinancialContract,
        thirdFinancialContract,
        forthFinancialContract,
        secondFinancialContract,
        secondNoiseFinancialContract
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
        firstNoiseFinancialContract,
        forthFinancialContract,
        thirdFinancialContract,
        fifthFinancialContract,
        secondFinancialContract,
        secondNoiseFinancialContract
      ))
      financialContracts <- repository.getFinancialContracts(3, 2)
    } yield {
      financialContracts should have length 1

      financialContracts.head shouldEqual fifthFinancialContract
    }
  }

  behavior of "deleting user financial contract"
  it should "delete the specified financial contract" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        firstNoiseFinancialContract,
        firstFinancialContract
      ))
      affectedRows <- repository.deleteFinancialContract(firstFinancialContract.id)
      financialContracts <- DBUtils.allFinancialContractsRows
    } yield {
      financialContracts should have length 1
      affectedRows shouldEqual 1
      financialContracts.head.id shouldEqual firstNoiseFinancialContract.id
    }
  }

  it should "NOT delete the specified financial contract when user missmatch" in {
    for {
      _ <- DBUtils.insertFinancialContracts(List(
        firstNoiseFinancialContract,
        firstFinancialContract
      ))
      affectedRows <- repository.deleteFinancialContract(firstFinancialContract.id)(UserBuilder().build)
      financialContracts <- DBUtils.allFinancialContractsRows
    } yield {
      financialContracts should have length 2
      affectedRows shouldEqual 0
    }
  }
}
