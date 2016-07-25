import sbt._

/* List the dependencies specific to the service here */
object ServiceDependencies {

  val hornetqClientVersion = "90"
  val hornetQClient = "com.hbc" %% "hornetq-client" % hornetqClientVersion
  val log4j2 = Seq(
    "org.bgee.log4jdbc-log4j2" % "log4jdbc-log4j2-jdbc4.1" % "1.16",
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.4.1",
    "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
    "org.apache.logging.log4j" % "log4j-core" % "2.4.1"
  )

  val serviceDependencies : Seq[ModuleID] = Seq(hornetQClient)
}
