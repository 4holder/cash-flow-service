package infrastructure

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._

@Singleton
class HealthCheckController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {
  def health: Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "Healthy"))
  }
}
