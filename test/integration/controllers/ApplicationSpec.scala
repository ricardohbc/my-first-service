package integration.controllers

import org.scalatest.{Matchers, WordSpec}
import play.api.test._
import play.api.Logger
import play.api.test.Helpers._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

class ApplicationSpec extends WordSpec with Matchers {

  import utils.TestUtils._

  "Application Controller" should {
    "send 404 on a bad request" in withPlay() {
      val result = route(application(), FakeRequest(GET, "/boom")).get
      status(result) shouldBe NOT_FOUND
    }

    "render the index page" in withPlay() {
      val index = route(application(), FakeRequest(GET, versionCtx + "/hbc-microservice-template")).get

      status(index) shouldBe OK
      contentType(index).get == "application/json" shouldBe true
      (contentAsJson(index) \ "response" \ "results").as[String] == "hbc-microservice-template is up and running!" shouldBe true
    }

    "get Swagger spec" in withPlay() {
      val index = route(application(), FakeRequest(GET, versionCtx + "/api-docs")).get

      status(index) shouldBe OK
      contentType(index).get == "application/json" shouldBe true
      (contentAsJson(index) \ "swagger").as[String] == "2.0" shouldBe true
    }

    "change the log Level" in withPlay() {
      val changeLog = route(application(), FakeRequest(GET, versionCtx + "/hbc-microservice-template/logLevel/WARN")).get
      status(changeLog) shouldBe OK
      contentType(changeLog).get == "application/json" shouldBe true
      (contentAsJson(changeLog) \ "response" \ "results").as[String] == "Log level changed to WARN" shouldBe true
      Logger.isDebugEnabled shouldBe false
      Await.result(route(application(), FakeRequest(GET, versionCtx + "/hbc-microservice-template/logLevel/DEBUG")).get, 10 seconds)
      Logger.isDebugEnabled shouldBe true
    }

    "not process incorrect log Level" in withPlay() {
      val result = route(application(), FakeRequest(GET, versionCtx + "/hbc-microservice-template/logLevel/WARN2")).get
      status(result) shouldBe NOT_FOUND
    }
  }
}
