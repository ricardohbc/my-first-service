
package filters

import javax.inject._

import akka.stream.Materializer
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

import scala.concurrent._

class ExceptionFilter @Inject() (@Named("versionURI") versionURI: String, val mat: Materializer) extends Filter {
  def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] =
    next(req) recover (helpers.ControllerPayloadLike.defaultExceptionHandler(versionURI)(req))
}

