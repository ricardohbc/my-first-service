package controllers

import play.api._
import play.api.mvc._

import helpers.{ControllerPayload, ControllerTimeout}
import scala.util.Try

import ch.qos.logback.classic.Level
import metrics.StatsDClient

object Application extends Controller
with ControllerTimeout
with ControllerPayload
with StatsDClient {

  def index = Action.async { request =>
    timeout(onHandlerRequestTimeout(request).as(JSON)) {
      val response = "hbc-microservice-template is up and running!"
      throw new NoSuchElementException("wut the hell, where is it?")
      writeResponseGet1(request, response)
    }
  }

  def changeLogLevel(levelString: String) = Action.async { request =>
    Logger.debug("hbc-microservice-template change log level called")
    timeout(onHandlerRequestTimeout(request).as(JSON)) {
      val level = Level.toLevel(levelString)
      Logger.underlyingLogger.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
      val response = Try(s"Log level changed to $level")
      writeResponseGet(request, response)
    }
  } 
}
