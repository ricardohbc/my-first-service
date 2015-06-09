package helpers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.language.postfixOps

trait ControllerTimeout extends ConfigHelper {
  val actionTimeout = config getInt "controllers.timeout" //millis

  def timeout[T <: Any](errorHandler: => T, time: Int = actionTimeout)(body: => T) : Future[T] = {
    val futureBody: Future[T] = Future {
      
    }

    val promise = Promise[T]()
    val timeoutFuture = play.api.libs.concurrent.Promise.timeout(errorHandler, time.millis)
  }
  
  def syncTimeout[T](errorHandler: => T, time: Int = actionTimeout)(body: => T) : Future[T] =
    timingoutFuture(errorHandler, time, Future(body))

  def asyncTimeout[T](errorHandler: => T, time: Int = actionTimeout, f: Future[T]) : Future[T] =
    timingoutFuture(errorHandler, time, f)

  private def timingoutFuture[T](errorHandler: => T, time: Int, f: Future[T]): Future[T] = {
    val promise = Promise[T]()
    val timeoutFuture = play.api.libs.concurrent.Promise.timeout(errorHandler, time.millis)
    promise.tryCompleteWith(f)
    promise.tryCompleteWith(timeoutFuture)
    promise future
  }
}
