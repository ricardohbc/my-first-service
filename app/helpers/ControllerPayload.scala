package helpers

import models._
import play.api.libs.json._
import play.api.mvc._
import scala.concurrent._
import play.api.libs.json.JsSuccess
import scala.util.control.NonFatal
import play.api.mvc.Result
import play.api.libs.json.JsResultException

trait ControllerPayload extends Controller {

  ////////////////////////
  //      RESPONSE      //
  ////////////////////////

  def writeResponseStore[T: Format](result: T, versionURI: String)(implicit request: Request[_]): Result =
    writeResponseSuccess(result, Created, versionURI)

  def writeResponseStores[T: Format](results: Seq[T], versionURI: String)(implicit request: Request[_]): Result =
    writeResponses(results, Created, versionURI)

  def writeResponseGet[T: Format](response: T, versionURI: String, errors: Seq[ApiErrorModel] = Seq())(implicit request: Request[_]): Result =
    writeResponseSuccess(response, Ok, versionURI, errors)

  def writeResponseUpdate[T: Format](result: T, versionURI: String)(implicit request: Request[_]): Result =
    writeResponseSuccess(result, Ok, versionURI)

  def writeResponseUpdates[T: Format](results: Seq[T], versionURI: String)(implicit request: Request[_]): Result =
    writeResponses(results, Ok, versionURI)

  def writeResponseRemove[T: Format](result: T, versionURI: String)(implicit request: Request[_]): Result =
    writeResponseSuccess(result, Ok, versionURI)

  def writeResponseSuccess[T: Format](result: T, status: Status, versionURI: String, errors: Seq[ApiErrorModel] = Seq())(implicit request: RequestHeader): Result =
    writeResponse(status, constructResponseModel(result, versionURI, errors))

  def writeResponseError(errors: Seq[ApiErrorModel], status: Status, versionURI: String)(implicit request: RequestHeader): Result =
    formatResponse(constructErrorResponseModel(errors, versionURI), status)

  def writeResponse(responseStatus: Status, body: ApiModel): Result =
    responseStatus.apply(Json.prettyPrint(Json.toJson(body))).as(JSON)

  def constructResultModel[T: Format](result: T): ApiResultModel = ApiResultModel(Json.toJson(result))

  def constructResponseModel[T: Format](
    result:     T,
    versionURI: String,
    errs:       Seq[ApiErrorModel] = Seq()
  )(implicit request: RequestHeader): ApiModel =
    ApiModel.apply(
      ApiRequestModel.fromReq(request, versionURI),
      constructResultModel(result),
      errs
    )

  def constructErrorResponseModel(errs: Seq[ApiErrorModel], versionURI: String)(implicit request: RequestHeader): ApiModel =
    ApiModel.apply(
      ApiRequestModel.fromReq(request, versionURI),
      EmptyApiResultModel,
      errs
    )

  private def formatResponse(responseModel: ApiModel, response: Status): Result =
    response.apply(Json.prettyPrint(Json.toJson(responseModel))).as(JSON)

  private def writeResponses[T: Format](
    results:    Seq[T],
    status:     Status,
    versionURI: String
  )(implicit request: Request[_]): Result =
    formatResponse(constructResponseModel(results, versionURI), status)

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

  def findResponseStatus(implicit request: RequestHeader): PartialFunction[Throwable, (Status, ApiErrorModel)] = {
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

  def handlerForRequest(versionURI: String)(implicit req: RequestHeader): (Status, ApiErrorModel) => Result = {
    (status, err) =>
      writeResponse(
        status,
        constructErrorResponseModel(Seq(err), versionURI)
      )
  }

  def defaultExceptionHandler(versionURI: String)(implicit req: RequestHeader): PartialFunction[Throwable, Result] =
    findResponseStatus andThen handlerForRequest(versionURI)(req).tupled
}

object ControllerPayloadLike extends ControllerPayload
