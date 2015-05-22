package controllers

import akka.actor.ActorSystem
import metrics.StatsDClient
import play.api._
import play.api.mvc._

import helpers.{ControllerPayload, ControllerTimeout}
import scala.util.Try

object Application extends Controller
with StatsDClient
with ControllerTimeout
with ControllerPayload {

  override val system = ActorSystem("hbc-microservice-template")

  def index = Action.async ({
    request =>
      increment("template_index")
      Logger.debug("Template index called")
      time("template_index_load_time") {
        timeout(onHandlerRequestTimeout(request).as(JSON)) {
          val response = Try("HBC Microservice is up and running!")
          writeResponseGet(request, response)
        }
      }
  })
}
