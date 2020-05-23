package unit.infrastructure

import infrastructure.HealthCheckController
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}


class HealthCheckControllerTest extends PlaySpec with Results {
  "healthy check controler must be OK" in {
    val controller = new HealthCheckController(Helpers.stubControllerComponents())
    val result = controller.health().apply(FakeRequest())

    contentType(result) mustBe Some("application/json")
    status(result) mustBe OK
  }
}
