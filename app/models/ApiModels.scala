package models

import java.text.SimpleDateFormat
import java.util.Calendar

import constants.Constants
import play.api.libs.json._
import play.api.mvc.RequestHeader


// results if we are successful
case class ApiResultModel(message: String, results: JsValue)

object ApiResultModel {
  implicit val resultFormat = Json.format[ApiResultModel]

  val defaultErrorModel = ApiResultModel(Constants.ERROR_MESSAGE, Json.toJson(JsNull))
}


// error info
case class ApiErrorModel(data: String, error: String)

object ApiErrorModel {
  implicit val errFormat = Json.format[ApiErrorModel]

  // it would be nicer if these were apply methods, but that f's up the implicit conversion
  // we could move the implicits somewhere else.  I tried but didn't care enough.  I wouldn't object if anyone wants to look into it!
  def fromException(ex: Throwable) = new ApiErrorModel(ex.getMessage, ex.getClass.getSimpleName)
  def fromExceptionAndMessage(message: String, ex: Throwable) = new ApiErrorModel(message, ex.getClass.getSimpleName)

  def defaultErrorModel(message: String) = fromException(new Exception(message))
}


// the request url
case class ApiRequestModel(url: String, server_received_time: String)

object ApiRequestModel {
  implicit val reqFormat = Json.format[ApiRequestModel]

  def fromReq(request: RequestHeader): ApiRequestModel = {
    val fullRequestUrl = "http://" + request.host + request.uri
    val df = new SimpleDateFormat(Constants.ZULU_DATE_FORMAT)
    new ApiRequestModel(
      fullRequestUrl,
      df.format(Calendar.getInstance.getTime)
    )
  }
}

// top level response structure
// I think it needs to go last to pick up the implicit  vals above...
case class ApiModel (request: ApiRequestModel, response: ApiResultModel, errors: Seq[ApiErrorModel])

object ApiModel {
  implicit val apiModelFormat = Json.format[ApiModel]

  // call it with request.body from WS api for example
  def fromBody(body: String): JsResult[ApiModel] = {
    val asJs: JsValue = Json.parse(body)
    asJs.validate[ApiModel]
  }

  // call this like this: ApiModel.resultsAs[List[String]](res.body)  => JsResult
  // obviously you need to specify the success type you're expecting  :)
  // https://www.playframework.com/documentation/2.3.x/api/scala/index.html#play.api.libs.json.JsResult
  // you can pattern match on JsSuccess/JsError, or map, flatMap, asOpt, asEither ...etc
  def resultsAs[A : Format](body: String): JsResult[A] =
    for {
      apiModel <- fromBody(body)
      result <- apiModel.response.results.validate[A]
    } yield result
  
  // maybe you just want to proxy the rest of the apiModel, but update the header
  def withHeader(body: String)(implicit req: RequestHeader): ApiModel = {
    val reqModel = ApiRequestModel.fromReq(req)
    fromBody(body)
      .fold(f => defaultErrorModel("unexpected failure parsing ApiModel\n${f.toString}"), _.copy(request = reqModel))
  }

  def defaultErrorModel(errorMessage: String)(implicit req: RequestHeader): ApiModel =
    ApiModel( ApiRequestModel.fromReq(req), 
              ApiResultModel.defaultErrorModel, 
              Seq(ApiErrorModel.defaultErrorModel(errorMessage)))

}
