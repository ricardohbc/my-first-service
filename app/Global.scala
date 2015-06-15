import scala.concurrent.Future
import play.api.mvc._
import play.api.GlobalSettings
import helpers.ControllerPayload

object Global extends GlobalSettings with ControllerPayload {

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(
      super.doFilter(next),
      ServiceFilters.TimingFilter,
      ServiceFilters.IncrementFilter,
      ServiceFilters.TimeoutFilter,
      ServiceFilters.ExceptionFilter
    )

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(getErrorFunction(writeResponseFailure)(request)(ex.getCause))

}
