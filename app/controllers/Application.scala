package controllers

import play.api._
import play.api.mvc._

import webservices.toggles.TogglesClient
import helpers.ControllerPayloadLike._
import helpers.ControllerTimeoutLike._

import ch.qos.logback.classic.Level

import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  @no.samordnaopptak.apidoc.ApiDoc(doc =
    """
    GET /hbc-microservice-template

    DESCRIPTION
      Check to see if hbc-microservice-template service is running

    RESULT
      Response

    Request: models.ApiRequestModel
      url: String
      server_received_time: String
      api_version: String
      help: String

    ResponseResult: models.ApiResultModel
      results: String

    Error: models.ApiErrorModel
      data: String
      error: String

    Response: models.ApiModel
      request: Request
      response: ResponseResult
      errors: Array Error
  """)
  def index = Action.async {
    implicit request =>
      timeout {
        val response = "hbc-microservice-template is up and running!"
        writeResponseGet(response)
      }
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc =

    """
    GET /hbc-microservice-template/logLevel/{level}

    DESCRIPTION
      Change the log level of this service

    PARAMETERS
      level: Enum(ALL, TRACE, DEBUG, INFO, WARN, ERROR, OFF) String <- The log level you want to set

    RESULT
      Response

  """)
  def changeLogLevelGet(levelString: String) = changeLogLevel(levelString)

  @no.samordnaopptak.apidoc.ApiDoc(doc =

    """
    PUT /hbc-microservice-template/logLevel/{level}

    DESCRIPTION
      Change the log level of this service

    PARAMETERS
      level: Enum(ALL, TRACE, DEBUG, INFO, WARN, ERROR, OFF) String <- The log level you want to set

    RESULT
      Response

  """)
  def changeLogLevel(levelString: String) = Action.async {
    implicit request =>
      timeout {
        Logger.debug("hbc-microservice-template change log level called")
        val level = Level.toLevel(levelString)
        Logger.underlyingLogger.asInstanceOf[ch.qos.logback.classic.Logger].setLevel(level)
        val response = s"Log level changed to $level"
        writeResponseGet(response)
      }
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc =

    """
    GET  /hbc-microservice-template/clear_toggles

    DESCRIPTION
      Clear the toggles cache, if you pass a toggle name under ?name=toggle_name it will clear that toggle, otherwise clear everything

    RESULT
      String

     """)
  def clearToggles(name: Option[String]) = Action.async {
    implicit request =>
      timeout {
        TogglesClient.clearCache(name)
        writeResponseGet("done!")
      }
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET  /hbc-microservice-template/toggles

    DESCRIPTION
      See what toggles our service has, if you pass a toggle name under ?name=toggle_name it will fetch that toggle, otherwise fetch everything

    RESULT
      ToggleResponse

    Toggle: models.Toggle
      toggle_name: String
      toggle_state: Boolean

    ToggleResult: models.ApiResultModel
      results: Array Toggle

    ToggleResponse: models.ApiModel
      request: Request
      response: ToggleResult
      errors: Array Error

     """) // This is useful for debugging, and perhaps pre-populating appropriate toggles ...
  def toggles(name: Option[String]) = Action.async {
    implicit request =>
      withTimeout {
        name.map(n => TogglesClient.getToggle(n).map(t => Seq(t.toList).flatten))
          .getOrElse(TogglesClient.getToggles())
          .map(r => writeResponseGet(r))
      }
  }
}
