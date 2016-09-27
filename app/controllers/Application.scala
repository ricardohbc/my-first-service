package controllers

import play.api._
import play.api.mvc._
import webservices.toggles.TogglesClientLike
import helpers.ControllerPayloadLike._
import javax.inject._

import no.samordnaopptak.json.J
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import ch.qos.logback.classic.Level
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.libs.json.JsObject

@Singleton
class Application @Inject() (
    timeoutHelper: helpers.ControllerTimeout,
    togglesClient: TogglesClientLike
) extends Controller {

  import timeoutHelper._

  @no.samordnaopptak.apidoc.ApiDoc(doc =
    """
    GET /v1/hbc-microservice-template

    DESCRIPTION
      Check to see if hbc-microservice-template service is running

    RESULT
      Response

    Request: models.ApiRequestModel
      url: String
      server_received_time: String
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
    GET /v1/hbc-microservice-template/logLevel/{level}

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
    PUT /v1/hbc-microservice-template/logLevel/{level}

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
    GET /v1/hbc-microservice-template/clear_toggles

    DESCRIPTION
      Clear the toggles cache, if you pass a toggle name under ?name=toggle_name it will clear that toggle, otherwise clear everything

    RESULT
      String

    """)
  def clearToggles(name: Option[String]) = Action.async {
    implicit request =>
      timeout {
        togglesClient.clearCache(name)
        writeResponseGet("done!")
      }
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET /v1/hbc-microservice-template/toggles

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
        name.map(n => togglesClient.getToggle(n).map(t => Seq(t.toList).flatten))
          .getOrElse(togglesClient.getToggles())
          .map(r => writeResponseGet(r))
      }
  }

  val swaggerInfoObject = J.obj(
    "info" -> J.obj(
      "title" -> "Generated Swagger API",
      "version" -> "1.0"
    )
  )

  /*@no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET /v1/api-docs

    DESCRIPTION
      Get main swagger json documentation
      You can add more detailed information here.
                                         """)*/
  def apiDoc = Action.async { implicit request =>
    try {
      val generatedSwaggerDocs = no.samordnaopptak.apidoc.ApiDocUtil.getSwaggerDocs("/")
      val json = generatedSwaggerDocs ++ swaggerInfoObject
      Future(Ok(json.asJsValue))
    } catch {
      case e: Exception =>
        println(s"ERROR: $e")
        throw e
    }
  }

  implicit val cl = getClass.getClassLoader
  private lazy val generator = SwaggerSpecGenerator() //TODO: need to update this when new models are added

  def specs = {
    Action.async { _ =>
      Future.fromTry(generator.generate())
        .map { newSwagger =>

          val keys = List("definitions", "paths")

          val oldSwagger = no.samordnaopptak.apidoc.ApiDocUtil.getSwaggerDocs("/").asJsValue

          val newSwaggerJsonKeyValMap = newSwagger.value
          val oldSwaggerJsonKeyValMap = oldSwagger.value

          val combinedPathDefs = for (key <- keys) yield {
            (
              key,
              newSwaggerJsonKeyValMap.get(key).map(_.as[JsObject])
              .foldLeft(oldSwaggerJsonKeyValMap.get(key).map(_.as[JsObject]).get)(_ ++ _)
            )
          }

          val unorganizedResult = newSwagger ++ JsObject(combinedPathDefs)
          val organizedKeys =
            List(
              "swagger",
              "info",
              "tags",
              "consumes",
              "produces",
              "paths",
              "definitions"
            )

          val organizedPairs = (for (key <- organizedKeys) yield {
            unorganizedResult.value.get(key) map (value => (key, value))
          }).flatten

          Ok(JsObject(organizedPairs))
        }
    }
  }

  def renderSwaggerUi = Action(Redirect("/v1/api-docs/swagger-ui/index.html?url=/v1/api-docs"))

}
