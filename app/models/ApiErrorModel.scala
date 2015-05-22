package models

import constants.Constants
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ApiErrorMessageModel(
  data: String,
  error: String
)

trait ApiError {
  implicit val errorMessageObjectWrites: Writes[ApiErrorMessageModel] = (
    (__ \ Constants.DATA).write[String] and
      (__ \ Constants.ERROR).write[String]
    )(unlift(ApiErrorMessageModel.unapply))
}

