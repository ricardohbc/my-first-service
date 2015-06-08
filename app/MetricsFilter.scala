import play.api.Play.current
//import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka
import play.api.mvc._
//import play.api.Routes
import scala.concurrent.Future
import play.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import metrics.StatsDClient._

// common logging and metrics for all requests
object MetricsFilter extends Filter {

	def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
		val startTime = System.currentTimeMillis
		val controllerActionTag = for {
			controller <- requestHeader.tags.get(play.api.Routes.ROUTE_CONTROLLER)
			action <- requestHeader.tags.get(play.api.Routes.ROUTE_ACTION_METHOD)
		} yield controller.replaceFirst("controllers.", "") + "." + action
		val reqTag = controllerActionTag.getOrElse(requestHeader.path)
		increment(reqTag)
		nextFilter(requestHeader).map { result =>
			val endTime = System.currentTimeMillis
			val respTime = (endTime - startTime).toInt
			timing(reqTag, respTime)
			//Logger.debug(s"FIlter: $reqTag took $respTime")
			result
		}
	}
}

