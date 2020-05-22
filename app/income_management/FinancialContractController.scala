package income_management

import java.util.UUID.randomUUID

import domain.{Amount, ContractType, User}
import income_management.models.financial_contract.{FinancialContract, FinancialContractRepository}
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext

class FinancialContractController @Inject()(
                                            cc: ControllerComponents,
                                            repository: FinancialContractRepository
                                           )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def registerNewFinancialContract: Action[AnyContent] = Action.async { _ =>

    val newFC = FinancialContract(
      id = randomUUID().toString,
      user = User(randomUUID().toString),
      name = "A Good Contract",
      contractType = ContractType.CLT,
      grossAmount = Amount.BRL(1235000),
      companyCnpj = Some("3311330900014"),
      startDate = DateTime.now,
      endDate = Some(DateTime.now),
      createdAt = DateTime.now,
      modifiedAt = DateTime.now
    )

    repository
      .insertContract(newFC)
      .map(fc =>

        Ok(Json.obj("id" -> fc.id,
          "user" -> fc.user.id,
          "name" -> fc.name,
          "contractType" -> fc.contractType.toString,
          "grossAmount" -> fc.grossAmount.valueInCents,
          "companyCnpj" -> fc.companyCnpj.get,
          "startDate" -> fc.startDate.toString,
          "endDate" -> fc.endDate.toString,
          "createdAt" -> fc.createdAt.toString,
          "modifiedAt" -> fc.modifiedAt.toString
        ))
      )
  }
}
