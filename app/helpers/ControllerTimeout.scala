package helpers

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import scala.language.postfixOps
import constants.Constants
import akka.pattern._
import javax.inject._

import akka.actor.ActorSystem
import globals.Contexts

class ControllerTimeout @Inject() (@Named("controllerTimeout") actionTimeout: Integer, system: ActorSystem) {

  // call these with some arbitrary blocking code
  def timeout[T](body: => T): Future[T] =
    timingoutFuture(actionTimeout.longValue, Future(body))

  def timeout[T](time: Long)(body: => T): Future[T] =
    timingoutFuture(time, Future(body))

  // call these if you already have a future
  def withTimeout[T](f: Future[T]): Future[T] =
    timingoutFuture(actionTimeout.longValue, f)

  def withTimeout[T](time: Long)(f: Future[T]): Future[T] =
    timingoutFuture(time, f)

  private def timingoutFuture[T](time: Long, f: Future[T]): Future[T] = {
    val timeoutFuture = after(time millis, using = system.scheduler)(Future.failed(new TimeoutException(Constants.TIMEOUT_MSG)))
    Future.firstCompletedOf(Seq(f, timeoutFuture))(Contexts.ctx)
  }
}

