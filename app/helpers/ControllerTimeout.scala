package helpers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.language.postfixOps

trait ControllerTimeout extends ConfigHelper {
  val actionTimeout = config getInt "controllers.timeout" //millis

  // If your code blocks, we'll just wrap it in a Future so we can time it out.  
  // This won't make things magically async or scalable.
  // If you really have long running synchronous code check your thread pool size at least.
  // E.g https://www.playframework.com/documentation/2.4.x/ThreadPools
  def syncTimeout[T](errorHandler: => T, time: Int = actionTimeout)(body: => T) : Future[T] =
    timingoutFuture(errorHandler, time, Future(body))

  // just call this if you already have a future from a nice async API, like reactivemongo  maybe.
  // this method isn't exactly necessary but I kind of want to try and 
  // force the user to pay some attention to blocking v actual non-blocking actions
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
