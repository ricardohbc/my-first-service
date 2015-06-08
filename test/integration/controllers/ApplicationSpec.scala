package integration.controllers

import play.api.test.Helpers._
import play.api.test._
import scala.concurrent.duration._
import play.api.{Logger, Play}
import play.api.test.FakeApplication
import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpec}
import scala.concurrent.Await
import scala.language.postfixOps

class ApplicationSpec extends WordSpec
with Matchers
with BeforeAndAfterAll {

  override def beforeAll() = {
    Play.start(FakeApplication())
  }

  override def afterAll() = {
    Play.stop()
  }

  "Application Controller" should {
    "send 404 on a bad request" in {
      route(FakeRequest(GET, "/boom")) shouldBe None
    }

    "render the index page" in {
      val index = route(FakeRequest(GET, "/hbc-microservice-template")).get

      status(index) shouldBe OK
      contentType(index).get == "application/json" shouldBe true
      (contentAsJson(index) \ "response" \ "results").as[String] == "hbc-microservice-template is up and running!" shouldBe true
    }

    "change the log Level" in {
      val changeLog = route(FakeRequest(GET, "/hbc-microservice-template/logLevel/WARN")).get
      status(changeLog) shouldBe OK
      contentType(changeLog).get == "application/json" shouldBe true
      (contentAsJson(changeLog) \ "response" \ "results").as[String] == "Log level changed to WARN" shouldBe true
      Logger.isDebugEnabled shouldBe false
      Await.result(route(FakeRequest(GET, "/hbc-microservice-template/logLevel/DEBUG")).get,10 seconds)
      Logger.isDebugEnabled shouldBe true
    }

    "ignore incorrect log Level" in {
      route(FakeRequest(GET, "/hbc-microservice-template/logLevel/WARN2")) shouldBe None
    }
  }
}
