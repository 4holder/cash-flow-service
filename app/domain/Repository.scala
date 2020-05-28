package domain

import org.joda.time.DateTime
import scala.concurrent.Future

trait Repository[MODEL<: RepositoryModel, PAYLOAD, PARENT <: RepositoryModel] {
  def all(page: Int, pageSize: Int)(implicit parent: PARENT): Future[Seq[MODEL]]
  def getById(id: String): Future[Option[MODEL]]
  def register(models: MODEL*): Future[Unit]
  def update(id: String, payload: PAYLOAD, now: DateTime = DateTime.now): Future[Int]
  def delete(id: String): Future[Int]

  protected def offset(page: Int, pageSize: Int): Int = {
    if(page <= 1)
      0
    else
      (page - 1) * pageSize
  }
}
