package unit.helpers

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import helpers.{ControllerPayload, ControllerTimeout}
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._
import play.api.Play
import play.api.libs.json.JsArray
import constants._

class ControllerTimeoutSpec extends WordSpec
with Matchers
with BeforeAndAfterAll
with ControllerTimeout
with ControllerPayload {

  override def beforeAll() = {
    Play.start(FakeApplication())
  }

  override def afterAll() = {
    Play.stop()
  }

  "ControllerTimeout" should {
    "return error on timeout" in {
      ((contentAsJson(
        timeout( writeResponseError(new TimeoutException(Constants.TIMEOUT_MSG))(FakeRequest(GET, "/microservice-template")).as(JSON)) {
          Thread.sleep(10000)
          Ok("Won't get here")
        }
      ) \ "errors").asInstanceOf[JsArray](0) \ "error").as[String] == "TimeoutException" shouldBe true
    }
    "return error on an async timeout" in {
      ((contentAsJson(
        withTimeout( writeResponseError(new TimeoutException(Constants.TIMEOUT_MSG))(FakeRequest(GET, "/microservice-template")).as(JSON)) {
          Future {
            Thread.sleep(10000)
            Ok("Won't get here")
          }
        }
      ) \ "errors").asInstanceOf[JsArray](0) \ "error").as[String] == "TimeoutException" shouldBe true
    }
  }
}
