package filters

import play.api.mvc._
import scala.concurrent._
import play.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import metrics.StatsDClient._
import helpers.ControllerPayload

// common logging and metrics for all requests
object ServiceFilters {

  object TimingFilter extends Filter {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = {
      time("", req) {
        next(req)
      }
    }
  }

  object IncrementFilter extends Filter {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = {
      val reqTag = requestTag(req)
      Logger.info(s"incrementing $reqTag")
      increment(reqTag)
      next(req)
    }
  }

  object ExceptionFilter extends Filter with ControllerPayload {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] =
      next(req) recover (defaultExceptionHandler(req))
  }
}
