package models

import java.text.SimpleDateFormat
import java.util.Calendar

import constants.Constants
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{AnyContent, Request}

case class ApiRequestModel(
  url: String,
  server_received_time: String
)

object ApiRequestModel {

  implicit val requestObjectWrites: Writes[ApiRequestModel] = (
    (__ \ Constants.URL).write[String] and
      (__ \ Constants.SERVER_RECEIVED_TIME).write[String]
    )(unlift(ApiRequestModel.unapply))

  def apply(request: Request[AnyContent]): ApiRequestModel = {
    val fullRequestUrl = "http://" + request.host + request.uri
    val df = new SimpleDateFormat(Constants.ZULU_DATE_FORMAT)
    new ApiRequestModel(
      fullRequestUrl,
      df.format(Calendar.getInstance.getTime)
    )
  }
}

