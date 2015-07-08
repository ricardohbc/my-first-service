package helpers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.language.postfixOps
import constants.Constants
import play.libs.Akka
import java.util.concurrent.TimeUnit

trait ControllerTimeout extends ConfigHelper {
  val actionTimeout = config getInt "controllers.timeout"

  // call this with some arbitrary blocking code 
  def timeout[T](time: Int = actionTimeout)(body: => T) : Future[T] =
    timingoutFuture(time, Future(body))

  // call this if you already have a future
  def withTimeout[T](time: Int = actionTimeout)(f: Future[T]) : Future[T] =
    timingoutFuture(time, f)

  private def timingoutFuture[T](time: Int, f: Future[T]): Future[T] = {
    val promise = Promise[T]()
    Akka.system.scheduler.scheduleOnce(FiniteDuration(time, TimeUnit.MILLISECONDS)) {
      promise.tryFailure(new TimeoutException(Constants.TIMEOUT_MSG))
    }
    promise.tryCompleteWith(f) future
  }
}
