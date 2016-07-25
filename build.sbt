import CommonDependencies._
import ServiceDependencies._
import net.virtualvoid.sbt.graph.Plugin._
import scalariform.formatter.preferences._

name := """hbc-microservice-template"""

version := "0.1"

envVars := Map("HBC_BANNER" -> "someBanner")

val defaultSettings: Seq[Setting[_]] = Seq(
      scalaVersion  := "2.11.7",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature","-Ywarn-unused-import"),
      libraryDependencies ++= commonDependencies
      ) ++ graphSettings


lazy val root = (project in file("."))
    .settings(defaultSettings: _*)
    .settings( libraryDependencies ++= serviceDependencies )
    .enablePlugins(PlayScala)
//    .disablePlugins(PlayLogback)

libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) }
//libraryDependencies ~= { _.map(_.exclude("ch.qos.logback", "logback-classic")) }

resolvers ++= Seq("Saks Artifactory - Ext Release Local" at "http://repo.saksdirect.com:8081/artifactory/ext-release-local",
	"Saks Artifactory - Libs Release Local" at "http://repo.saksdirect.com:8081/artifactory/libs-release-local",
	"Saks Artifactory - Libs Release" at "http://repo.saksdirect.com:8081/artifactory/libs-release"
	)
		   
lazy val buildAll = TaskKey[Unit]("build-all", "Compiles and runs all tests")
lazy val buildZip = TaskKey[Unit]("build-zip","Compiles, tests, and publishes a zip file with the new code.")
lazy val preCommit = TaskKey[Unit]("pre-commit", "Compiles, tests, zips code, and then refreshes docker container.")

buildAll <<= Seq(clean, compile in Compile, compile in Test, test in Test).dependOn

buildZip <<= ((packageBin in Universal) map { out =>
  println("Copying Zip file to docker directory.")
  IO.move(out, file(out.getParent + "/../../docker/" + out.getName))
}).dependsOn(buildAll)


preCommit := {"./refresh-service.sh"!}

preCommit <<= preCommit.dependsOn(buildZip)

scalariformSettings

ScalariformKeys.preferences := FormattingPreferences()
  .setPreference( AlignParameters, true )
  .setPreference( AlignSingleLineCaseStatements, true )
  .setPreference( DoubleIndentClassDeclaration, true )
