package globals

import filters.ServiceFilters
import play.api.mvc._
import play.api.GlobalSettings
import helpers.{ControllerPayload, TimingoutAction, ConfigHelper}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import play.api.mvc._
import play.api.mvc.BodyParsers
import play.libs.Akka
import constants.Constants

class GlobalServiceSettings extends GlobalSettings with ControllerPayload with ConfigHelper {

  val actionTimeout = config.getInt("controllers.timeout")

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(
      super.doFilter(new TimingoutAction(next, actionTimeout)),
      ServiceFilters.TimingFilter,
      ServiceFilters.IncrementFilter,
      ServiceFilters.ExceptionFilter
    )

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(InternalServerError("This shouldn't happen"))

}
