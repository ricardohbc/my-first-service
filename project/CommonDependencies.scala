import sbt._

/* List the dependencies that are common across all microservices
 * DO NOT list dependencies that are specific to a microservice. Use 'ServiceDependencies' instead. */
object CommonDependencies {

  val scalaTestVersion = "2.2.5"
  val scalaCheckVersion = "1.12.2"
  val apiDocVersion = "14"
  val playWSVersion = "2.3.9"
  val playMockWSVersion = "2.3.0"
  val sprayVersion = "1.3.3"

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test, it, ct, ft"
  val apiDoc = "com.hbc" %% "api_doc" % apiDocVersion
  val scalacheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test, it, ct, ft"
  val playWS = "com.typesafe.play" %% "play-ws" % playWSVersion
  val playMockWS = "de.leanovate.play-mockws" %% "play-mockws" % playMockWSVersion % "test, it, ct, ft"
  val sprayCaching = "io.spray" %% "spray-caching" % sprayVersion

  val commonDependencies : Seq[ModuleID] =
    Seq(
      playWS,
      playMockWS,
      scalaTest,
      apiDoc,
      scalacheck,
      sprayCaching
    )
}
