import sbt._

/* List the dependencies specific to the service here */
object ServiceDependencies {

  val hornetqClientVersion = "90"
  val hornetQClient = "com.hbc" %% "hornetq-client" % hornetqClientVersion

  val serviceDependencies : Seq[ModuleID] = Seq(hornetQClient)
}
