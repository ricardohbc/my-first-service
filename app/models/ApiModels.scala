package models

import java.text.SimpleDateFormat
import java.util.Calendar

import constants.Constants
//import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AnyContent, RequestHeader}

// top level response structure
case class ApiModel (request: ApiRequestModel, response: ApiResultModel, errors: ApiErrorModel)

object ApiModel {
  implicit val apiModelFormat = Json.format[ApiModel]
}


// results if we are successful
case class ApiResultModel(message: String, result: JsValue)

object ApiResultModel {
  implicit val resultFormat = Json.format[ApiResultModel]
}


// ****** errors and messages
case class ApiErrorModel(data: String, error: String)

object ApiErrorModel {

  //def fromException(ex: Throwable) = new ApiErrorModel(ex.getMessage, ex.getClass.getSimpleName)

  //def fromExceptionAndMessage(message: String, ex: Throwable) = new ApiErrorModel(message, ex.getClass.getSimpleName)
  
  implicit val errFormat = Json.format[ApiErrorModel]
}


// ***** the request url
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