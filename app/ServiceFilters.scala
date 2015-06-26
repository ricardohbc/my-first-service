import play.api.mvc._
import scala.concurrent.Future
import play.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import metrics.StatsDClient
import helpers.{ControllerTimeout, ControllerPayload}

// common logging and metrics for all requests
object ServiceFilters {

  object TimingFilter extends Filter with StatsDClient {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = {
      val reqTag = requestTag(req)
      time("", req) {
        Logger.info("TRIGGER SOME CODE IN THE TIMED BLOCK")
        next(req)
      }
    }
  }

  object IncrementFilter extends Filter with StatsDClient {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = {
      val reqTag = requestTag(req)
      Logger.info(s"incrementing $reqTag")
      increment(reqTag)
      next(req)
    }
  }

  object TimeoutFilter extends Filter with ControllerTimeout with ControllerPayload {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = 
      withTimeout( onHandlerRequestTimeout(req).as(JSON) ) {
        next(req)
      }
  }

  object ExceptionFilter extends Filter
      with ControllerPayload {
    def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = {
      next(req) recover (findResponseHandler andThen {case exceptionInfo => responseExec(exceptionInfo)(req)})
    }
  }
}
