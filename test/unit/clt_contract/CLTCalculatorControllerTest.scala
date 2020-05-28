package unit.clt_contract

import clt_contract.{CLTCalculatorController, CLTContractCalculatorService}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.Helpers.{contentAsJson, contentType, status, _}
import play.api.test.{FakeRequest, Helpers}

class CLTCalculatorControllerTest extends PlaySpec with Results {

  private val controller = new CLTCalculatorController(
    Helpers.stubControllerComponents(),
    new CLTContractCalculatorService()
  )

  "calculate CLT Contract" in {
    val grossSalary = 900000
    val body = s"""{
                 |  "grossSalary": $grossSalary,
                 |  "dependentQuantities": 1,
                 |  "deductionsAmount": 50000
                 |}""".stripMargin
    val request = FakeRequest()
      .withMethod("POST")
      .withBody(Json.parse(body))

    val result = controller.calculateCLTContract().apply(request)

    contentType(result) mustBe Some("application/json")
    status(result) mustEqual OK
    val jsonContent = contentAsJson(result)

    (jsonContent \ "grossSalary" \ "valueInCents").as[Long] mustEqual 900000
    (jsonContent \ "grossSalary" \ "currency").as[String] mustEqual "BRL"
    ((jsonContent \ "incomes").head \ "incomeType").as[String] mustEqual "SALARY"
    ((jsonContent \ "incomes")(1) \ "incomeType").as[String] mustEqual "THIRTEENTH_SALARY"
    ((jsonContent \ "incomes").last \ "incomeType").as[String] mustEqual "THIRTEENTH_SALARY_ADVANCE"
  }
}
