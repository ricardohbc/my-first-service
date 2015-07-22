package integration.controllers

import play.api.test.Helpers._
import play.api.test._
import play.api.Play
import org.scalatest.{ Matchers, BeforeAndAfterAll, WordSpec }
import play.api.test.FakeApplication
import scala.Some

class HystrixMetricsStreamSpec extends WordSpec
    with Matchers
    with BeforeAndAfterAll {

  override def beforeAll() = {
    Play.start(FakeApplication())
  }

  override def afterAll() = {
    Play.stop()
  }

  "HystrixMetricsStream controller" should {
    "send json" in {
      val hystrix = route(FakeRequest(GET, "/hbc-microservice-template/admin/hystrix-demo")).get
      status(hystrix) shouldBe OK
    }
  }
}