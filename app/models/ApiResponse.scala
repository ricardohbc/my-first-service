package models

import constants.Constants
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ApiResponseModel (
  request: JsValue,
  response: JsValue,
  errors: JsValue
)

object ApiResponseModel {
  implicit val apiResponseSchemaWrites: Writes[ApiResponseModel] = (
    (__ \ Constants.REQUEST).write[JsValue] and
      (__ \ Constants.RESPONSE).write[JsValue] and
      (__ \ Constants.ERRORS).write[JsValue]
    )(unlift(ApiResponseModel.unapply))
}

