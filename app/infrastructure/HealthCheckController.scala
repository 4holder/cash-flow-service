package infrastructure

import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import play.api.mvc._
import slick.jdbc.JdbcProfile

@Singleton
class HealthCheckController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {
  def health: Action[AnyContent] = Action {
    Ok(Json.obj("status" -> "Healthy"))
  }
}
