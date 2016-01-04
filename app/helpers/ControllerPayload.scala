package helpers

import constants.Constants._
import models._

import play.api.libs.json._
import play.api.mvc._
import play.api.libs.json.JsSuccess
import scala.util.control.NonFatal
import play.api.mvc.Result
import play.api.libs.json.JsResultException

import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

trait ControllerPayload extends Controller {

  ////////////////////////
  //      RESPONSE      //
  ////////////////////////

  def writeResponseStore[T: Format](result: T)(implicit request: Request[_]): Result =
    writeResponseSuccess(result, Created)

  def writeResponseStores[T: Format](results: Seq[T])(implicit request: Request[_]): Result =
    writeResponses(results, Created)

  def writeResponseGet[T: Format](response: T, errors: Seq[ApiErrorModel] = Seq())(implicit request: Request[_]): Result =
    writeResponseSuccess(response, Ok, errors)

  def writeResponseUpdate[T: Format](result: T)(implicit request: Request[_]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseUpdates[T: Format](results: Seq[T])(implicit request: Request[_]): Result =
    writeResponses(results, Ok)

  def writeResponseRemove[T: Format](result: T)(implicit request: Request[_]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseSuccess[T: Format](result: T, status: Status, errors: Seq[ApiErrorModel] = Seq())(implicit request: RequestHeader): Result =
    writeResponse(status, constructResponseModel(result, errors))

  def writeResponseError(errors: Seq[ApiErrorModel], status: Status)(implicit request: RequestHeader): Result =
    formatResponse(constructErrorResponseModel(errors), status)

  def writeResponse(responseStatus: Status, body: ApiModel): Result =
    responseStatus.apply(Json.prettyPrint(Json.toJson(body))).as(JSON)

  def writeResponseData[T](responseData: Future[ResponseData], validate: (JsValue) => JsResult[T], processBody: T => T = identity _)(implicit request: Request[_], typeFormat: Format[T]): Future[Result] = {
    responseData.map {
      case SuccessfulResponse(body, cookie) =>
        validate(body) match {
          case JsSuccess(c, _) => writeResponseGet(processBody(c)).withCookies(cookie: _*)
          case JsError(e)      => writeResponseError(Seq(ApiErrorModel("Failed to validate JSON", "JSON error")), Status(500))
        }
      case FailureResponse(errors, code) => writeResponseError(errors, Status(code))
    }
  }

  def writeResponseData(responseData: Future[ResponseData])(implicit request: Request[_]): Future[Result] = {
    responseData.map {
      case SuccessfulResponse(body, cookie) =>
        writeResponseGet(body).withCookies(cookie: _*)
      case FailureResponse(errors, code) => writeResponseError(errors, Status(code))
    }
  }

  def constructResultModel[T: Format](result: T): ApiResultModel = ApiResultModel(Json.toJson(result))

  def constructResponseModel[T: Format](
    result: T,
    errs:   Seq[ApiErrorModel] = Seq()
  )(implicit request: RequestHeader): ApiModel =
    ApiModel.apply(
      ApiRequestModel.fromReq(request),
      constructResultModel(result),
      errs
    )

  def constructErrorResponseModel(errs: Seq[ApiErrorModel])(implicit request: RequestHeader): ApiModel =
    ApiModel.apply(
      ApiRequestModel.fromReq(request),
      EmptyApiResultModel,
      errs
    )

  private def formatResponse(responseModel: ApiModel, response: Status): Result =
    response.apply(Json.prettyPrint(Json.toJson(responseModel))).as(JSON)

  private def writeResponses[T: Format](
    results: Seq[T],
    status:  Status
  )(implicit request: Request[_]): Result =
    formatResponse(constructResponseModel(results), status)

  ////////////////////////
  //     GET ITEMS      //
  ////////////////////////

  def getRequestItem[T: Format](implicit request: Request[AnyContent]): T = {
    val readJsonObject: Format[JsValue] = (__ \ "item").format[JsValue]
    getRequestBodyAsJson(request).validate(readJsonObject) match {
      case JsError(e) => throw new JsResultException(e)
      case JsSuccess(hbcStatusObject, _) =>
        //Validate the hbcStatus object
        hbcStatusObject.validate[T] match {
          case JsSuccess(hbcStatus, _) => hbcStatus
          case JsError(e)              => throw new JsResultException(e)
        }
    }
  }

  def getRequestItems[T: Format](implicit request: Request[AnyContent]): Seq[T] = {
    val readJsonObject: Format[Seq[JsValue]] = (__ \ "items").format[Seq[JsValue]]
    getRequestBodyAsJson(request).validate(readJsonObject) match {
      case JsError(e) => throw new JsResultException(e)
      case JsSuccess(hbcStatusObjectList, _) =>
        hbcStatusObjectList.map(hbcStatusObject =>
          hbcStatusObject.validate[T] match {
            case JsSuccess(hbcStatus, _) => hbcStatus
            case JsError(e)              => throw new JsResultException(e)
          })
    }
  }

  ////////////////////////
  //      HELPERS       //
  ////////////////////////

  private def getRequestBodyAsJson(implicit request: Request[AnyContent]): JsValue =
    request.body.asJson.fold(throw new IllegalArgumentException("no json found"))(x => x)

  val findResponseStatus: PartialFunction[Throwable, (Status, ApiErrorModel)] = {
    case e: NoSuchElementException =>
      (NotFound, ApiErrorModel.fromExceptionAndMessage(
        "hbcStatus '" + e.getMessage + "' does not exist.", e
      ))
    case e: VerifyError =>
      (PreconditionFailed, ApiErrorModel.fromException(e))
    case e: ClassCastException =>
      (UnsupportedMediaType, ApiErrorModel.fromException(e))
    case e: IllegalArgumentException =>
      (BadRequest, ApiErrorModel.fromException(e))
    case e: JsResultException =>
      (BadRequest, ApiErrorModel.fromException(e))
    case e: TimeoutException =>
      (ServiceUnavailable, ApiErrorModel.fromException(e))
    case NonFatal(e) =>
      (InternalServerError, ApiErrorModel.fromExceptionAndMessage(
        "Yikes! An error has occurred: " + e.getMessage, e
      ))
  }

  def handlerForRequest(implicit req: RequestHeader): (Status, ApiErrorModel) => Result = {
    (status, err) =>
      writeResponse(
        status,
        constructErrorResponseModel(Seq(err))
      )
  }

  def getJsessionId(implicit request: Request[AnyContent]): Option[String] = request.cookies.get(JSESSIONID).map(_.value)

  def getUserName(implicit request: Request[AnyContent]): Option[String] = request.cookies.get(USERNAME).map(_.value)

  def defaultExceptionHandler(req: RequestHeader): PartialFunction[Throwable, Result] =
    findResponseStatus andThen handlerForRequest(req).tupled
}

object ControllerPayloadLike extends ControllerPayload
