package integration.controllers

import play.api.test.Helpers._
import play.api.test._
import utils.TestGlobal
import scala.concurrent.duration._
import play.api.{ Logger, Play }
import play.api.test.FakeApplication
import org.scalatest.{ Matchers, BeforeAndAfterAll, WordSpec }
import scala.concurrent.Await
import scala.language.postfixOps
import utils.TestUtils._

class ApplicationSpec extends WordSpec
    with Matchers
    with BeforeAndAfterAll {

  override def beforeAll() = {
    Play.start(FakeApplication(withGlobal = Some(TestGlobal)))
  }

  override def afterAll() = {
    Play.stop()
  }

  "Application Controller" should {
    "send 404 on a bad request" in {
      route(FakeRequest(GET, "/boom")) shouldBe None
    }

    "render the index page" in {
      val index = route(FakeRequest(GET, versionCtx + "/service")).get

      status(index) shouldBe OK
      contentType(index).get == "application/json" shouldBe true
      (contentAsJson(index) \ "response" \ "results").as[String] == "service is up and running!" shouldBe true
    }

    "get Swagger spec" in {
      val index = route(FakeRequest(GET, versionCtx + "/api-docs")).get

      status(index) shouldBe OK
      contentType(index).get == "application/json" shouldBe true
      (contentAsJson(index) \ "swagger").as[String] == "2.0" shouldBe true
    }

    "change the log Level" in {
      val changeLog = route(FakeRequest(GET, versionCtx + "/service/logLevel/WARN")).get
      status(changeLog) shouldBe OK
      contentType(changeLog).get == "application/json" shouldBe true
      (contentAsJson(changeLog) \ "response" \ "results").as[String] == "Log level changed to WARN" shouldBe true
      Logger.isDebugEnabled shouldBe false
      Await.result(route(FakeRequest(GET, versionCtx + "/service/logLevel/DEBUG")).get, 10 seconds)
      Logger.isDebugEnabled shouldBe true
    }

    "ignore incorrect log Level" in {
      route(FakeRequest(GET, versionCtx + "/service/logLevel/WARN2")) shouldBe None
    }
  }
}
