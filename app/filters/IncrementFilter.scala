package filters

import javax.inject.Inject

import akka.stream.Materializer
import metrics.StatsDClientLike
import play.api.Logger
import play.api.mvc._

import scala.concurrent._

class IncrementFilter @Inject() (recorder: StatsDClientLike, val mat: Materializer) extends Filter {
  def apply(next: RequestHeader => Future[Result])(req: RequestHeader): Future[Result] = {
    val reqTag = recorder.requestTag(req)
    Logger.info(s"incrementing $reqTag")
    recorder.increment(reqTag)
    next(req)
  }
}
