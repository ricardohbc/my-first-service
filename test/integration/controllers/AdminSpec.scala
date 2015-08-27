package integration.controllers

import play.api.test.Helpers._
import play.api.test._
import play.api.Play
import org.scalatest.{ Matchers, BeforeAndAfterAll, WordSpec }
import play.api.test.FakeApplication
import utils.TestGlobal
import utils.TestUtils._

class AdminSpec extends WordSpec
    with Matchers
    with BeforeAndAfterAll {

  override def beforeAll() = {
    Play.start(FakeApplication(withGlobal = Some(TestGlobal)))
  }

  override def afterAll() = {
    Play.stop()
  }

  "Admin controller" should {
    "show **pong** when /hbc-microservice-template/admin/ping endpoint is called" in {
      val ping = route(FakeRequest(GET, versionCtx + "/hbc-microservice-template/admin/ping")).get

      status(ping) shouldBe OK
      contentAsString(ping).contains("pong") shouldBe true
    }

    "show **JVM Stats** when /hbc-microservice-template/admin/jvmstats endpoint is called" in {
      val jvmstats = route(FakeRequest(GET, versionCtx + "/hbc-microservice-template/admin/jvmstats")).get

      status(jvmstats) shouldBe OK
      contentAsString(jvmstats).contains("jvm_num_cpus") shouldBe true
    }
  }
}
