package controllers

import play.api._
import play.api.mvc._

import helpers.{ControllerPayload, ControllerTimeout}

import ch.qos.logback.classic.Level

object Application extends Controller
with ControllerTimeout
with ControllerPayload {

  def index = Action { implicit request =>
    val response = "hbc-microservice-template is up and running!"
    writeResponseGet(response)
  }

  def changeLogLevel(levelString: String) = Action { implicit request =>
    Logger.debug("hbc-microservice-template change log level called")
    val level = Level.toLevel(levelString)
    Logger.underlyingLogger.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
    val response = s"Log level changed to $level"
    writeResponseGet(response)
  }
}
