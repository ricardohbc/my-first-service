import play.PlayScala

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

name := """hbc-microservice-template"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Saks Artifactory - Release" at "http://repo.saksdirect.com:8081/artifactory/libs-release"

libraryDependencies ++= {
  val scalaTestVersion = "2.2.5"
  Seq(
  	"org.scalacheck" %% "scalacheck" % "1.12.2" % "test",
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )
}

net.virtualvoid.sbt.graph.Plugin.graphSettings

