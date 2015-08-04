package unit.filters

import _root_.helpers.ConfigHelper
import play.api.mvc._
import scala.concurrent._
import play.api.mvc.Results._
import play.api.test.FakeRequest
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import play.api.Play
import play.api.test.Helpers._
import play.api.http.HttpVerbs.GET
import globals.GlobalServiceSettings
import scala.concurrent.duration._
import scala.language.postfixOps
import play.api.test.FakeApplication
import scala.Some
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.Json

object TestGlobal extends GlobalServiceSettings

class FiltersSpec
    extends WordSpec
    with Matchers
    with BeforeAndAfterAll
    with ConfigHelper {

  val actionTimeout = config getInt "controllers.timeout"

  val testRouter: PartialFunction[(String, String), Handler] = {
    case (GET, "/slowRequest") =>
      Action {
        Thread.sleep(actionTimeout * 3)
        Ok("Should never get here")
      }
    case (GET, "/errorRequest") =>
      Action {
        throw new NullPointerException("Bad code!")
        Ok("Should never get here")
      }
  }

  override def beforeAll() = {
    Play.start(FakeApplication(withGlobal = Some(TestGlobal), withRoutes = testRouter))
  }

  override def afterAll() = {
    Play.stop()
  }

  "ServiceFilters" should {

    "return TimeoutException after configured time" in {
      val result: Result = Await.result(route(FakeRequest(GET, "/slowRequest")).get, (actionTimeout * 2) millis)
      val bytesContent = Await.result(result.body |>>> Iteratee.consume[Array[Byte]](), Duration.Inf)
      val contentAsJson = Json.parse(new String(bytesContent))
      result.header.status shouldBe 503
      ((contentAsJson \ "errors")(0) \ "error").as[String] == "TimeoutException" shouldBe true
    }

    "handle exception when it's thrown by controller" in {
      val result = route(FakeRequest(GET, "/errorRequest")).get
      ((contentAsJson(result) \ "errors")(0) \ "error").as[String] == "NullPointerException" shouldBe true
    }
  }
}