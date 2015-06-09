package controllers

import play.api._
import play.api.mvc._

import helpers.{ControllerPayload, ControllerTimeout}
import scala.util.Try

import ch.qos.logback.classic.Level

object Application extends Controller
with ControllerTimeout
with ControllerPayload {

  def index = Action { request =>
    val response = Try("hbc-microservice-template is up and running!")
    writeResponseGet(request, response)
  }

  def changeLogLevel(levelString: String) = Action { request =>
    Logger.debug("hbc-microservice-template change log level called")
    val level = Level.toLevel(levelString)
    Logger.underlyingLogger.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
    val response = Try(s"Log level changed to $level")
    writeResponseGet(request, response)
  }
}
