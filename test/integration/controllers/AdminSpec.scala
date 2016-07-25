package scala.controllers

import org.scalatest.{Matchers, BeforeAndAfterAll, WordSpec}
import play.api.test._
import play.api.test.Helpers._
import play.api.Play

class AdminSpec extends WordSpec
    with Matchers with BeforeAndAfterAll {

  import utils.TestUtils._

  val app = application()

  override def beforeAll() = {
    Play.start(app)
  }

  override def afterAll() = {
    Play.stop(app)
  }

  "Admin controller" should {
    "return healthcheck status" in {
      val ping = route(app, FakeRequest(GET, versionCtx + "/hbc-microservice-template/admin/ping")).get

      status(ping) shouldBe OK
      (contentAsJson(ping) \ "response" \ "results").as[String] shouldBe "pong"
    }

    "show **JVM Stats** when /hbc-microservice-template/admin/jvmstats endpoint is called" in {
      val jvmstats = route(app, FakeRequest(GET, versionCtx + "/hbc-microservice-template/admin/jvmstats")).get

      status(jvmstats) shouldBe OK
      contentAsString(jvmstats).contains("jvm_num_cpus") shouldBe true
    }
  }
}
