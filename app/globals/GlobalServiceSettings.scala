package globals

import filters.ServiceFilters
import play.api.mvc._
import play.api.GlobalSettings
import helpers.ControllerPayload

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scala.concurrent.duration._
import play.api.mvc._
import play.api.mvc.BodyParsers
import play.libs.Akka
import akka.pattern._
import constants.Constants

class GlobalServiceSettings extends GlobalSettings with ControllerPayload {

  class TOAction(ea: EssentialAction) extends Action[AnyContent] {
    override def parser = BodyParsers.parse.anyContent

    override def apply(req: Request[AnyContent]): Future[Result] = {
      val goodResult: Future[Result] = ea.apply(req).run
      val timeoutFuture = after(1000.millis, using = Akka.system.scheduler)(Future.successful(Ok("oh no too slow")))
      Future.firstCompletedOf(Seq(timeoutFuture, goodResult))
    }
  }

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(
      super.doFilter(new TOAction(next)),
      ServiceFilters.TimingFilter,
      ServiceFilters.IncrementFilter,
      ServiceFilters.ExceptionFilter
    //ServiceFilters.TimeoutFilter
    )

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(InternalServerError("This shouldn't happen"))

}
