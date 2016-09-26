import play.core.PlayVersion
import sbt._

/* List the dependencies that are common across all microservices
 * DO NOT list dependencies that are specific to a microservice. Use 'ServiceDependencies' instead. */
object CommonDependencies {

  val scalaTestVersion = "2.2.6"
  val scalaCheckVersion = "1.12.5"
  val playWSVersion = PlayVersion.current
  val sprayVersion = "1.3.3"
  val doppelaugeVersion = "1.1.3"

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalacheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test"
  val playWS = "com.typesafe.play" %% "play-ws" % playWSVersion
  val sprayCaching = "io.spray" %% "spray-caching" % sprayVersion
  val doppelauge = "com.github.sun-opsys" % "doppelauge" % doppelaugeVersion
  val playSwagger = "com.iheart" %% "play-swagger" % "0.3.2-PLAY2.5"
  val swaggerUi = "org.webjars" % "swagger-ui" % "2.1.4"

  val commonDependencies : Seq[ModuleID] =
    Seq(
      playWS,
      scalaTest,
      scalacheck,
      sprayCaching,
      doppelauge,
      playSwagger,
      swaggerUi
    )
}
