package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import play.api.mvc.Cookie

trait CookieSerializer {
  implicit val cookieReads: Reads[Cookie] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "value").read[String] and
    (JsPath \ "max_age").readNullable[Int] and
    (JsPath \ "path").read[String] and
    (JsPath \ "domain").readNullable[String] and
    (JsPath \ "secure").read[Boolean] and
    (JsPath \ "http_only").read[Boolean]
  )(Cookie.apply _)

  implicit val cookieWrites: Writes[Cookie] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "value").write[String] and
    (JsPath \ "max_age").writeNullable[Int] and
    (JsPath \ "path").write[String] and
    (JsPath \ "domain").writeNullable[String] and
    (JsPath \ "secure").write[Boolean] and
    (JsPath \ "http_only").write[Boolean]
  )(unlift(Cookie.unapply))

  implicit val cookieFormat: Format[Cookie] =
    Format(cookieReads, cookieWrites)
}

trait ResponseData

case class SuccessfulResponse(
  body:    JsValue,
  cookies: Seq[Cookie] = Seq.empty
) extends ResponseData

case class FailureResponse(
  errors: Seq[ApiErrorModel],
  code:   Int
) extends ResponseData

object SuccessfulResponse extends CookieSerializer {
  implicit val successfulResponseFormat: Format[SuccessfulResponse] = Json.format[SuccessfulResponse]
}

object FailureResponse {
  implicit val failureResponseFormat: Format[FailureResponse] = Json.format[FailureResponse]
}

object ResponseData extends CookieSerializer {
  implicit val responseDataReads = {
    val success = Json.reads[SuccessfulResponse]
    val failure = Json.reads[FailureResponse]
    __.read[SuccessfulResponse](success).map(x => x: ResponseData) |
      __.read[FailureResponse](failure).map(x => x: ResponseData)
  }

  implicit val responseDataWrites = Writes[ResponseData] { responseData =>
    responseData match {
      case success: SuccessfulResponse => Json.writes[SuccessfulResponse].writes(success)
      case failure: FailureResponse    => Json.writes[FailureResponse].writes(failure)
    }
  }
}
