import play.PlayScala
import CommonDependencies._
import net.virtualvoid.sbt.graph.Plugin._
import ServiceDependencies._
import sbt.Keys._
import scalariform.formatter.preferences._

name := """hbc-microservice-template"""

version := "0.1"

lazy val IntegrationTestAltConf = config("it") extend(Test)
lazy val ContractTestConf = config("ct") extend(Test)
lazy val FunctionalTestConf = config("ft").extend(Test)

val defaultSettings: Seq[Setting[_]] = Seq(
      scalaVersion  := "2.11.6",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature","-Ywarn-unused-import"),
      libraryDependencies ++= commonDependencies
      ) ++ graphSettings


lazy val root = (project in file("."))
    .configs(IntegrationTestAltConf, ContractTestConf, FunctionalTestConf)
    .settings(defaultSettings: _*)
    .settings(inConfig(IntegrationTestAltConf)(Defaults.itSettings): _*)
    .settings(inConfig(ContractTestConf)(Defaults.testSettings): _*)
    .settings(inConfig(ContractTestConf)(Defaults.testTasks): _*)
    .settings(inConfig(FunctionalTestConf)(Defaults.testSettings): _*)
    .settings(inConfig(FunctionalTestConf)(Defaults.testTasks): _*)
    .settings(
        libraryDependencies ++= serviceDependencies
       )
    .enablePlugins(PlayScala)

resolvers ++= Seq("Saks Artifactory - Ext Release Local" at "http://repo.saksdirect.com:8081/artifactory/ext-release-local",
	"Saks Artifactory - Libs Release Local" at "http://repo.saksdirect.com:8081/artifactory/libs-release-local",
	"Saks Artifactory - Libs Release" at "http://repo.saksdirect.com:8081/artifactory/libs-release"
	)

lazy val cleanBuild = TaskKey[Unit]("clean-build", "Cleans code and builds project. Note clean will take more time than normal sbt build.")
lazy val integration = TaskKey[Unit]("integration", "Runs all Integration Tests.")
lazy val functional = TaskKey[Unit]("functional", "Runs all Functional Tests.")
lazy val contract = TaskKey[Unit]("contract", "Runs all Contract Tests.")
lazy val testAll = TaskKey[Unit]("test-all", "Runs all Integration, Functional, and Unit Tests.")
lazy val buildAll = TaskKey[Unit]("build-all", "Compiles and runs all unit and integration tests.")
lazy val buildZip = TaskKey[Unit]("build-zip","Publishes a zip file with the new code.")
lazy val deployBuild = TaskKey[Unit]("deploy-build", "Builds the distribution zip, and deploys the current code for our service.")
lazy val preCommit = TaskKey[Unit]("pre-commit", "Compiles, tests, zips code, refreshes docker container, and then runs integration tests.")

cleanBuild := Def.sequential(clean, compile in Compile, compile in Test, compile in IntegrationTest).value

integration := (test in IntegrationTest).value

functional := (test in FunctionalTestConf).value

contract := Def.sequential(compile in ContractTestConf, test in ContractTestConf).value

testAll := Def.sequential(test in Test, integration, functional).value

buildAll := Def.sequential(cleanBuild, testAll).value

buildZip <<= ((packageBin in Universal) map { out =>
  println("Copying Zip file to docker directory.")
  IO.move(out, file(out.getParent + "/../../docker/" + out.getName))
})

deployBuild := ("./refresh-service.sh"!)
deployBuild <<= deployBuild.dependsOn(buildZip)

preCommit := Def.sequential(cleanBuild, test in Test, deployBuild, integration, functional).value

def integrationTestFilter:String => Boolean ={ name => (name endsWith "IntegrationSpec")}
def functionalTestFilter:String => Boolean ={ name => (name endsWith "FunctionalSpec")}
def contractTestFilter:String => Boolean ={ name => (name endsWith "ContractSpec")}
def unitTestFilter(name: String): Boolean = ((name endsWith "Spec") && !integrationTestFilter(name) && !contractTestFilter(name) && !functionalTestFilter(name))


testOptions in IntegrationTest := Seq(Tests.Filter(integrationTestFilter))
sourceDirectory in IntegrationTest <<= baseDirectory(_ / "test")
testOptions in Test := Seq(Tests.Filter(unitTestFilter))

testOptions in ContractTestConf := Seq(Tests.Filter(contractTestFilter))
sourceDirectory in ContractTestConf <<= baseDirectory(_ / "test/contract")
sourceDirectories in ContractTestConf <+= baseDirectory(_ / "test/contract")
scalaSource in ContractTestConf <<= baseDirectory(_ / "test/contract")

testOptions in FunctionalTestConf := Seq(Tests.Filter(functionalTestFilter))
sourceDirectory in FunctionalTestConf <<= baseDirectory(_ / "test/functional")
sourceDirectory in FunctionalTestConf <<= baseDirectory(_ / "test/functional")
scalaSource in FunctionalTestConf <<= baseDirectory(_ / "test/functional")

scalariformSettings

ScalariformKeys.preferences := FormattingPreferences()
  .setPreference( AlignParameters, true )
  .setPreference( AlignSingleLineCaseStatements, true )
  .setPreference( DoubleIndentClassDeclaration, true )
