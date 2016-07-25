
package webservices.toggles

import javax.inject.Inject

import com.typesafe.config.ConfigFactory
import org.scalatest.WordSpec
import models._

import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs.ws.WSClient

object ToggleServiceSpec {
  val configString = """
                       | webservices {
                       |   toggles {
                       |     url="http://hd1dtgl01lx.saksdirect.com:9880/toggle-service/toggles"
                       |   }
                       | }
                     """.stripMargin

  val fallbackConfig = ConfigFactory.parseString(configString)

  val fullConfig = ConfigFactory.load().withFallback(fallbackConfig)

  lazy val testUrl: String = fullConfig.getString("webservices.toggles.url")
}

//@RunWith(classOf[JUnitRunner])
class ToggleServiceSpec @Inject() (ws: WSClient) extends WordSpec with org.scalatest.Matchers {

  val toggle1 = Toggle("TEST_ONE", false)
  val toggle2 = Toggle("TEST_TWO", true)
  val toggle3 = Toggle("TEST_THREE", true)

  val client = new TogglesClient(ToggleServiceSpec.testUrl, ws)

  client.addToCache(toggle1)
  client.addToCache(toggle2)
  client.addToCache(toggle3)

  "Toggle service with spray cache" should {
    "return a toggle already in the cache" in {
      val toggleFuture = client.getToggle("TEST_ONE")
      val toggle = Await.result(toggleFuture, 1.seconds)
      toggle.get.toggle_state shouldBe false
    }

    "delete a single toggle" should {
      val initialSize = client.toggleCache.size
      client.clearCache(Some("TEST_TWO"))
      client.toggleCache.size shouldBe (initialSize - 1)
    }

    "cache should be clearable" in {
      client.clearCache(None)
      client.toggleCache.size shouldBe 0
    }
  }

}
