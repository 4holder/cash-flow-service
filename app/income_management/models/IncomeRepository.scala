package income_management.models

import com.google.inject.Singleton

import scala.concurrent.Future

@Singleton
class IncomeRepository {
  def registerIncome(income: Any): Future[Income] = ???
}
