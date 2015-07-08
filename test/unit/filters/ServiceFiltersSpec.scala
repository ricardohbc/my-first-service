package unit.filters

import _root_.helpers.ConfigHelper
import play.api.mvc._
import scala.concurrent._
import play.api.mvc.Results._
import play.api.test.{FakeRequest, FakeApplication}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import play.api.Play
import play.api.test.Helpers._
import play.api.http.HttpVerbs.GET
import scala.Some
import globals.GlobalServiceSettings
import scala.concurrent.duration._
import scala.language.postfixOps

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
        Ok("Hi")
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
      Await.result(route(FakeRequest(GET, "/slowRequest")).get, (actionTimeout * 2) millis)
    }
  }
}