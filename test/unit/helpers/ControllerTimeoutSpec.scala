package unit.helpers

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import helpers.{ ControllerPayload, ControllerTimeout }
import scala.concurrent.duration._
import scala.language.postfixOps
import play.api.Play
import play.api.test.FakeApplication

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
      intercept[TimeoutException](
        Await.result(
          timeout() {
            Thread.sleep(10000)
            Ok("Won't get here")
          },
          10 seconds
        )
      )
    }
    "return error on an async timeout" in {
      intercept[TimeoutException](
        Await.result(
          withTimeout() {
            Future {
              Thread.sleep(10000)
              Ok("Won't get here")
            }
          },
          10 seconds
        )
      )
    }
  }
}
