package controllers

import play.api._
import play.api.mvc._

import helpers.{ControllerPayload, ControllerTimeout}

import ch.qos.logback.classic.Level

object Application extends Controller
with ControllerTimeout
with ControllerPayload {

  @no.samordnaopptak.apidoc.ApiDoc(doc="""
    GET /hbc-microservice-template

    DESCRIPTION
      Check to see if hbc-microservice-template service is running

    RESULT
      Response

    Request: models.ApiRequestModel
      url: String
      server_received_time: String

    ResponseResult: !
      message: String
      results: String

    Error: models.ApiErrorModel
      data: String
      error: String

    Response: models.ApiModel
      request: Request
      response: ResponseResult
      errors: Array Error
  """)
  def index = Action { implicit request =>
    val response = "hbc-microservice-template is up and running!"
    writeResponseGet(response)
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc="""
    GET /hbc-microservice-template/logLevel/{level}

    DESCRIPTION
      Change the log level of this service

    PARAMETERS
      level: Enum(ALL, TRACE, DEBUG, INFO, WARN, ERROR, OFF) String <- The log level you want to set

    RESULT
      Response

  """)
  def changeLogLevelGet(levelString: String) = changeLogLevel(levelString)

  @no.samordnaopptak.apidoc.ApiDoc(doc="""
    PUT /hbc-microservice-template/logLevel/{level}

    DESCRIPTION
      Change the log level of this service

    PARAMETERS
      level: Enum(ALL, TRACE, DEBUG, INFO, WARN, ERROR, OFF) String <- The log level you want to set

    RESULT
      Response

  """)
  def changeLogLevel(levelString: String) = Action { implicit request =>
    Logger.debug("hbc-microservice-template change log level called")
    val level = Level.toLevel(levelString)
    Logger.underlyingLogger.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
    val response = s"Log level changed to $level"
    writeResponseGet(response)
  }
}
