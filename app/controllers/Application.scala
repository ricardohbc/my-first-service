package controllers

import play.api.Play.current
//import akka.actor.ActorSystem
import play.api.libs.concurrent.Akka
import metrics.StatsDClient
import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Akka

import helpers.{ControllerPayload, ControllerTimeout}
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import ch.qos.logback.classic.Level
import metrics.StatsDClient
import play.api.Play.current
import play.api.libs.concurrent.Akka


object Application extends Controller
with StatsDClient
with ControllerTimeout
with ControllerPayload {

  override val system = Akka.system

  def index = Action.async { request => 
      //increment("hbc-microservice-template_index")
      Logger.debug("hbc-microservice-template index called")
      
      //time("hbc-microservice-template_index_load_time") {
        timeout(onHandlerRequestTimeout(request).as(JSON)) {
          //Thread.sleep(1000) 
          val response = Try("hbc-microservice-template is up and running!")
          writeResponseGet(request, response)
        }
      //}
  }

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
