import sbt._

/* List the dependencies that are common across all microservices
 * DO NOT list dependencies that are specific to a microservice. Use 'ServiceDependencies' instead. */
object CommonDependencies {

  val scalaTestVersion = "2.2.5"
  val scalaCheckVersion = "1.12.2"

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"

  val scalacheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test"

  val commonDependencies : Seq[ModuleID] = Seq(scalaTest,scalacheck)
}
