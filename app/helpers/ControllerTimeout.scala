package helpers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.language.postfixOps
import constants.Constants
import play.libs.Akka
import akka.pattern._
import globals.Contexts

trait ControllerTimeout extends ConfigHelper {
  val actionTimeout = config getInt "controllers.timeout"

  // call these with some arbitrary blocking code
  def timeout[T](body: => T): Future[T] =
    timingoutFuture(actionTimeout, Future(body))

  def timeout[T](time: Long)(body: => T): Future[T] =
    timingoutFuture(time, Future(body))

  // call these if you already have a future
  def withTimeout[T](f: Future[T]): Future[T] =
    timingoutFuture(actionTimeout, f)

  def withTimeout[T](time: Long)(f: Future[T]): Future[T] =
    timingoutFuture(time, f)

  private def timingoutFuture[T](time: Long, f: Future[T]): Future[T] = {
    val timeoutFuture = after(time millis, using = Akka.system.scheduler)(Future.failed(new TimeoutException(Constants.TIMEOUT_MSG)))
    Future.firstCompletedOf(Seq(f, timeoutFuture))(Contexts.ctx)
  }
}

object ControllerTimeoutLike extends ControllerTimeout
