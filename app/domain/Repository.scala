package domain

trait Repository {
  protected def offset(page: Int, pageSize: Int): Int = {
    if(page <= 1)
      0
    else
      (page - 1) * pageSize
  }
}
