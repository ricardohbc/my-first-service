package controllers

import akka.actor.ActorSystem
import com.s5a.metrics.{MetricNamespace, MetricName}
import play.api._
import play.api.mvc._


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class ApplicationLike (val recorder: RecorderLike) extends Controller with RecorderTrait {

  def index = Action.async ({
    request =>
      increment("template_test")

      Logger.debug("test logging")
      time("hbcStatusControllerLike_storeStatus") {
        Future(
          Ok(views.html.index("Your new application is ready."))
        )
      }
  })

}

object Application extends ApplicationLike (
  MetricRecorder(ActorSystem("hbc-microservice-template"), MetricNamespace.SERVICE_CORE))