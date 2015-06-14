package helpers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.language.postfixOps

trait ControllerTimeout extends ConfigHelper {
  val actionTimeout = config getInt "controllers.timeout"

  // call this with some arbitrary blocking code 
  def timeout[T](errorHandler: => T, time: Int = actionTimeout)(body: => T) : Future[T] =
    timingoutFuture(errorHandler, time, Future(body))

  // call this if you already have a future
  def withTimeout[T](errorHandler: => T, time: Int = actionTimeout)(f: Future[T]) : Future[T] =
    timingoutFuture(errorHandler, time, f)

  private def timingoutFuture[T](errorHandler: => T, time: Int, f: Future[T]): Future[T] = {
    val promise = Promise[T]()
    val timeoutFuture = play.api.libs.concurrent.Promise.timeout(errorHandler, time millis)
    promise.tryCompleteWith(f)
    promise.tryCompleteWith(timeoutFuture)
    promise future
  }
}
