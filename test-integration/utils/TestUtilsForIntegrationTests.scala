package utils

import akka.actor.ActorSystem
import com.google.inject.name.Names
import com.typesafe.config.ConfigFactory
import play.api.inject.{Binding, bind}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Play}

import scala.concurrent._
import duration._

object TestUtilsForIntegrationTests {
  val configString = """
                       | application.secret="SECRET"
                       | logger.application=ERROR
                       |
                       | mock.simulate-order = false
                       | mock.simulate-confirmation = true
                       |
                       | bf-address  = "https://api-sandbox.borderfree.com"
                       | bf-username = "saksoff5th_amb_stage"
                       | bf-password = "YN0TInXA"
                       |
                       | play.http.filters=filters.ServiceFilters
                       | controllers.timeout=10000
                       | akka {
                       |   akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
                       |   loglevel = ERROR
                       | }
                     """.stripMargin

  val config = ConfigFactory.parseString(configString)

  val configuration = new Configuration(config)

  val defaultBindings: Seq[Binding[_]] = List[Binding[_]](
    bind[Duration].qualifiedWith("requestTimeout").toInstance(60.seconds),
    bind[String].qualifiedWith("bf-address").toInstance(configuration.getString("bf-address").get),
    bind[String].qualifiedWith("bf-username").toInstance(configuration.getString("bf-username").get),
    bind[String].qualifiedWith("bf-password").toInstance(configuration.getString("bf-password").get),
    bind(classOf[helpers.ControllerTimeout]).toSelf,
    bind(classOf[metrics.StatsDClientLike]).toInstance(metrics.NoOpStatsDClient),
    bind(classOf[webservices.toggles.TogglesClientLike]).toInstance(webservices.toggles.TogglesClient()),
    bind(classOf[filters.TimingFilter]).toSelf,
    bind(classOf[filters.IncrementFilter]).toSelf,
    bind(classOf[filters.ExceptionFilter]).toSelf
  )

  def application(
    config:   Configuration   = configuration,
    bindings: Seq[Binding[_]] = defaultBindings
  ): Application = {
    val builder = new GuiceApplicationBuilder().configure(config)
    bindings.foldLeft(builder)(_.overrides(_)).build
  }

  def withPlay[T](app: Application = application())(body: => T): T = {
    Play.start(app)
    try {
      body
    } finally {
      Play.stop(app)
    }
  }

}
