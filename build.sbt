import play.PlayScala

name := """hbc-microservice-template"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Saks Artifactory - Release" at "http://repo.saksdirect.com:8081/artifactory/libs-release"

libraryDependencies ++= {
  val saksLoggerVersion = "65-76"
  val saksMetricsVersion = "55"
  val saksConfigVersion = "38"
  Seq(
    "com.s5a" % "SaksLogger" % saksLoggerVersion,
    "com.s5a" % "metrics" % saksMetricsVersion,
    "com.s5a" % "SaksConfig" % saksConfigVersion,
    jdbc,
    anorm,
    cache,
    ws
  )
}

net.virtualvoid.sbt.graph.Plugin.graphSettings

