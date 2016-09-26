package controllers

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import play.api.Play
import play.api.test.Helpers._
import play.api.test._

class AdminSpec extends WordSpec
    with Matchers with BeforeAndAfterAll {

  import utils.TestUtilsForUnitTests._

  val app = application()

  override def beforeAll() = {
    Play.start(app)
  }

  override def afterAll() = {
    Play.stop(app)
  }

  "Admin controller" should {
    "return healthcheck status" in {
      val ping = route(app, FakeRequest(GET, "/v1/hbc-microservice-template/admin/ping")).get

      status(ping) shouldBe OK
      (contentAsJson(ping) \ "response" \ "results").as[String] shouldBe "pong"
    }

    "show **JVM Stats** when /hbc-microservice-template/admin/jvmstats endpoint is called" in {
      val jvmstats = route(app, FakeRequest(GET, "/v1/hbc-microservice-template/admin/jvmstats")).get

      status(jvmstats) shouldBe OK
      contentAsString(jvmstats).contains("jvm_num_cpus") shouldBe true
    }
  }
}
