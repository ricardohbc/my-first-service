package integration.controllers

import play.api.test.Helpers._
import play.api.test._
import play.api.Play
import scala.Some
import play.api.test.FakeApplication
import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpec}

class ApplicationSpec extends WordSpec
with Matchers
with BeforeAndAfterAll {

  override def beforeAll() = {
    Play.start(FakeApplication(withGlobal = Some(new controllers.Global)))
  }

  override def afterAll() = {
    Play.stop()
  }

  "Application Controller" should {
    "send 404 on a bad request" in {
      route(FakeRequest(GET, "/boom")) shouldBe None
    }

    "render the index page" in {
      val index = route(FakeRequest(GET, "/microservice-template")).get

      status(index) shouldBe OK
      contentType(index).get == "application/json" shouldBe true
      (contentAsJson(index) \ "response" \ "results").as[String] == "HBC Microservice is up and running!" shouldBe true
    }
  }
}
