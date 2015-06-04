package controllers

import play.api.Play.current
//import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka
import metrics.StatsDClient
import play.api._
import play.api.mvc._

import helpers.{ControllerPayload, ControllerTimeout}
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

import ch.qos.logback.classic.Level
import metrics.StatsDClient
import play.api.Play.current
//import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka

object Tracking extends StatsDClient {
  override val system = Akka.system
  import play.Logger
  def apply[A](tag: String)(action: Action[A]) =  Action.async(action.parser) { request =>
    val startTime = System.currentTimeMillis
    action(request).map{ resp =>
      val endTime = System.currentTimeMillis
      val respTime = (endTime - startTime).toInt
      timing(tag, respTime)
      Logger.debug(s"req $tag took $respTime.")
      resp
    }
  }
}

object Application extends Controller
with StatsDClient
with ControllerTimeout
with ControllerPayload {

  override val system = Akka.system //ActorSystem("hbc-microservice-template")

  def index = Tracking("app-index-route-tag")( Action.async { request =>
      //increment("hbc-microservice-template_index")
      Logger.debug("hbc-microservice-template index called")
      Thread.sleep(1000)
      //time("hbc-microservice-template_index_load_time") {
        timeout(onHandlerRequestTimeout(request).as(JSON)) {
          val response = Try("hbc-microservice-template is up and running!")
          writeResponseGet(request, response)
        }
      //}
  })

  def changeLogLevel(levelString: String) = Action.async ({
    request =>
      increment("hbc-microservice-template_change_log_level")
      Logger.debug("hbc-microservice-template change log level called")
      time("hbc-microservice-template_change_log_level_load_time") {
        timeout(onHandlerRequestTimeout(request).as(JSON)) {
          val level = Level.toLevel(levelString)
          Logger.underlyingLogger.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
          val response = Try(s"Log level changed to $level")
          writeResponseGet(request, response)
        }
      }
  })  
}
