import scala.concurrent.Future
import play.api._
import play.api.mvc._
import play.api.GlobalSettings
import helpers.ControllerPayloadLike

object Global extends GlobalSettings {

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(super.doFilter(next), ServiceFilters.TimingFilter, ServiceFilters.IncrementFilter, ServiceFilters.TimeoutFilter)

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(ControllerPayloadLike.writeResponseFailure(request, ex.getCause))

}
