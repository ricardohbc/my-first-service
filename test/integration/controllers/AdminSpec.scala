package integration.controllers

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithBrowser

@RunWith(classOf[JUnitRunner])
class AdminSpec extends Specification {

  "Admin controller" should {

    "show **pong** when /admin/ping endpoint is called" in new WithBrowser {

      browser.goTo("http://localhost:" + port + "/admin/ping")

      browser.pageSource must contain("pong")
    }

    "show **JVM Stats** when /admin/jvmstats endpoint is called" in new WithBrowser {
      println("bla")
      browser.goTo("http://localhost:" + port + "/admin/jvmstats")

      browser.pageSource must contain("jvm_num_cpus")
    }
  }
}
