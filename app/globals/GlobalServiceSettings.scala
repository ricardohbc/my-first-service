package globals

import filters.ServiceFilters
import play.api.GlobalSettings
import helpers.{ControllerPayload, ConfigHelper}
import scala.concurrent._
import play.api.mvc._

class GlobalServiceSettings extends GlobalSettings with ControllerPayload with ConfigHelper {

  val actionTimeout = config.getInt("controllers.timeout")

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(
      super.doFilter(next),
      ServiceFilters.TimingFilter,
      ServiceFilters.IncrementFilter,
      ServiceFilters.ExceptionFilter
    )

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(InternalServerError("This shouldn't happen"))

}
