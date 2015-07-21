package actions

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scala.concurrent.duration._
import play.api.mvc._
import play.api.libs.concurrent.Promise
import play.libs.Akka
import akka.pattern._
import constants.Constants

object TimingOutAction extends ActionBuilder[Request] with Results {
  def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
    val timeoutFuture = after(1000.millis, using = Akka.system.scheduler)(Future.successful(RequestTimeout("oh no too slow")))
    val goodResult = block(request)
    Future.firstCompletedOf(Seq(timeoutFuture, goodResult))
  }
}