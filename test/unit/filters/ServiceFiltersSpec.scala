package unit.filters

import javax.inject._
import play.api.inject.bind
import org.scalatest.{Matchers, WordSpec}
import play.api._
import play.api.http.HttpVerbs.GET
import play.api.mvc._
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers._
import com.typesafe.config.ConfigFactory

class CrashyAdmin @Inject() (
    timeoutHelper: helpers.ControllerTimeout
) extends controllers.Admin(timeoutHelper) {

  override def ping = Action.async {
    implicit request =>
      throw new NullPointerException("Test!")
  }
}

class SlowAdmin @Inject() (
    timeoutHelper: helpers.ControllerTimeout
) extends controllers.Admin(timeoutHelper) {

  import timeoutHelper._

  override def ping = Action.async {
    implicit request =>
      timeout {
        Thread.sleep(5000)
        writeResponseGet("pong")
      }
  }
}

object FiltersSpec {
  val configString =
    """
      | controllers.timeout=50
    """.stripMargin

  val config = ConfigFactory.parseString(configString).withFallback(utils.TestUtilsForUnitTests.config)

  val configuration = new Configuration(config)
}

class FiltersSpec
    extends WordSpec
    with Matchers {

  import utils.TestUtilsForUnitTests._

  val crashyBindings = defaultBindings ++ Seq(bind[controllers.Admin].to[CrashyAdmin])
  val slowBindings = defaultBindings ++ Seq(bind[controllers.Admin].to[SlowAdmin])

  "ServiceFilters" should {
    "handle exception when it's thrown by controller" in withPlay(application(bindings = crashyBindings)) {
      val ping = route(FakeRequest(GET, "/v1/my-first-service/admin/ping")).get
      ((contentAsJson(ping) \ "errors")(0) \ "error").as[String] == "NullPointerException" shouldBe true
    }

    "return TimeoutException after configured time" in withPlay(application(config = FiltersSpec.configuration, bindings = slowBindings)) {
      val ping = route(FakeRequest(GET, "/v1/my-first-service/admin/ping")).get
      ((contentAsJson(ping) \ "errors")(0) \ "error").as[String] == "TimeoutException" shouldBe true
      status(ping) shouldBe 503
    }
  }
}
