package globals

import filters.ServiceFilters
import scala.concurrent.Future
import play.api.mvc._
import play.api.GlobalSettings
import helpers.ControllerPayload

class GlobalServiceSettings extends GlobalSettings with ControllerPayload {

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(
      super.doFilter(next),
      ServiceFilters.TimingFilter,
      ServiceFilters.IncrementFilter,
      ServiceFilters.ExceptionFilter,
      ServiceFilters.TimeoutFilter
    )

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(InternalServerError("This shouldn't happen"))

}
