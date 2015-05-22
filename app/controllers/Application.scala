package controllers

import akka.actor.ActorSystem
import metrics.StatsDClient
import play.api._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller
with StatsDClient {

  override val system = ActorSystem("hbc-microservice-template")

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
