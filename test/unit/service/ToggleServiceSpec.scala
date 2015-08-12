package webservices.toggles

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import models._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

//@RunWith(classOf[JUnitRunner])
class ToggleServiceSpec extends WordSpec with org.scalatest.Matchers {

  val toggle1 = Toggle("TEST_ONE", false)
  val toggle2 = Toggle("TEST_TWO", true)
  val toggle3 = Toggle("TEST_THREE", true)

  TogglesClient.addToCache(toggle1)
  TogglesClient.addToCache(toggle2)

  "Toggle service with spray cache" should {
    "return a toggle already in the cache" in {
      val toggleFuture = TogglesClient.getToggle("TEST_ONE")
      val toggle = Await.result(toggleFuture, 1.seconds)
      toggle.toggle_state shouldBe false
    }

    "delete a single toggle" should {
      val initialSize = TogglesClient.toggleCache.size
      TogglesClient.clearCache(Some("TEST_TWO"))
      TogglesClient.toggleCache.size shouldBe (initialSize - 1)
    }

    "cache should be clearable" in {
      TogglesClient.clearCache(None)
      TogglesClient.toggleCache.size shouldBe 0
    }
  }

}