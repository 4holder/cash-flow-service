package utils

import org.scalatest.{AsyncFlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

trait AsyncUnitSpec extends AsyncFlatSpec with Matchers with MockitoSugar
