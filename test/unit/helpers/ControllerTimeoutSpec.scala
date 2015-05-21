package unit.helpers

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import helpers.{ControllerPayload, ControllerTimeout}
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._
import play.api.Play
import play.api.libs.json.JsArray

class ControllerTimeoutSpec extends WordSpec
with Matchers
with BeforeAndAfterAll
with ControllerTimeout
with ControllerPayload {

  override def beforeAll() = {
    Play.start(FakeApplication(withGlobal = Some(new controllers.Global)))
  }

  override def afterAll() = {
    Play.stop()
  }

  "ControllerTimeout" should {
    "return error on timeout" in {
      ((contentAsJson(
        timeout(
          onHandlerRequestTimeout(
            FakeRequest(GET, "/microservice-template")).as(JSON)
        )
        {
        Thread.sleep(10000)
        Ok("Won't get here")
        }
      ) \ "errors").asInstanceOf[JsArray](0) \ "error").as[String] == "TimeoutException" shouldBe true
    }
  }
}
