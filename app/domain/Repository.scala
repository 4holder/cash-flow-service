package domain

import scala.concurrent.Future

trait Repository {
  def belongsToUser(id: String, user: User): Future[Boolean]
  def parentBelongsToUser(parentId: String, user: User): Future[Boolean] = {
    Future.successful(true)
  }

  protected def offset(page: Int, pageSize: Int): Int = {
    if(page <= 1)
      0
    else
      (page - 1) * pageSize
  }
}
