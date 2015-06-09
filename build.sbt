import play.PlayScala
import CommonDependencies._
import net.virtualvoid.sbt.graph.Plugin._
import ServiceDependencies._

name := """hbc-microservice-template"""

version := "0.1"

val defaultSettings: Seq[Setting[_]] = Seq(
      scalaVersion  := "2.11.6",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
      libraryDependencies ++= commonDependencies
      ) ++ graphSettings


lazy val root = (project in file("."))
    .settings(defaultSettings: _*)
    .settings(
        libraryDependencies ++= serviceDependencies
       )
    .enablePlugins(PlayScala)

resolvers += "Saks Artifactory - Release" at "http://repo.saksdirect.com:8081/artifactory/libs-release"


