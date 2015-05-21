package helpers

import com.typesafe.config.ConfigFactory
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._

trait ControllerTimeout {
  lazy val config = ConfigFactory.load()
  var actionTimeout = config getInt "controllers.timeout" millis

  def timeout[T](errorHandler: => T)(body: => T) : Future[T] = {
    val promise = Promise[T]()
    val futureBody = scala.concurrent.Future {
      body
    }
    val timeoutFuture = play.api.libs.concurrent.Promise.timeout(errorHandler, actionTimeout)
    promise.tryCompleteWith(futureBody)
    promise.tryCompleteWith(timeoutFuture)
    promise future
  }
}
