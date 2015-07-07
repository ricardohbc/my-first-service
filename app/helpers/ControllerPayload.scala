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

  def writeResponseStore[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Created)

  def writeResponseStores[T : Format](results: Seq[T])(implicit request: Request[AnyContent]): Result =
    writeResponses(results, Created)

  def writeResponseGet[T : Format](response: T, errors: Seq[ApiErrorModel] = Seq())(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(response, Ok, errors)

  def writeResponseUpdate[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseUpdates[T : Format](results: Seq[T])(implicit request: Request[AnyContent]): Result =
    writeResponses(results, Ok)

  def writeResponseRemove[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseSuccess[T : Format](result: T, status: Status, errors: Seq[ApiErrorModel] = Seq())(implicit request: RequestHeader): Result =
    writeResponse(status, constructResponseModel(result, errors))

  def writeResponseError(errors: Seq[ApiErrorModel], status: Status)(implicit request: RequestHeader): Result =
    formatResponse(constructErrorResponseModel(errors), status)

  def writeResponse(responseStatus: Status, body: ApiModel): Result =
    responseStatus.apply(Json.prettyPrint(Json.toJson(body))).as(JSON)

  def constructResultModel[T: Format](result: T): ApiResultModel = ApiResultModel(Json.toJson(result))

  def constructResponseModel[T : Format](
    result: T,
    errs: Seq[ApiErrorModel] = Seq())(implicit request: RequestHeader): ApiModel =
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

  private def writeResponses[T : Format](
      results: Seq[T],
      status: Status)(implicit request: Request[AnyContent]): Result =
    formatResponse(constructResponseModel(results), status)

  ////////////////////////
  //     GET ITEMS      //
  ////////////////////////

  def getRequestItem[T: Format](request: Request[AnyContent]): T = {
    val readJsonObject: Format[JsValue] = (__ \ "item").format[JsValue]
    getRequestBodyAsJson(request).validate(readJsonObject) match {
      case JsError(e) => throw new JsResultException(e)
      case JsSuccess(hbcStatusObject, _) =>
        //Validate the hbcStatus object
        hbcStatusObject.validate[T] match {
          case JsSuccess(hbcStatus, _) => hbcStatus
          case JsError(e) => throw new ClassCastException("Could not cast input into proper type")
        }
    }
  }

  def getRequestItems[T: Format](request: Request[AnyContent]): Seq[T] = {
    val readJsonObject: Format[Seq[JsValue]] = (__ \ "items").format[Seq[JsValue]]
    getRequestBodyAsJson(request).validate(readJsonObject) match {
      case JsError(e) => throw new JsResultException(e)
      case JsSuccess(hbcStatusObjectList, _) =>
        hbcStatusObjectList.map(hbcStatusObject =>
          hbcStatusObject.validate[T] match {
            case JsSuccess(hbcStatus, _) => hbcStatus
            case JsError(e) => throw new ClassCastException("Could not cast '" + hbcStatusObject + "' into proper type")
          }
        )
    }
  }

  ////////////////////////
  //      HELPERS       //
  ////////////////////////


  private def getRequestBodyAsJson(request: Request[AnyContent]): JsValue =
    request.body.asJson.fold(throw new IllegalArgumentException("no json found"))(x => x)

  val findResponseStatus: PartialFunction[Throwable, (Status, ApiErrorModel)] = {
    case e: NoSuchElementException =>
      (NotFound, ApiErrorModel.fromExceptionAndMessage(
        "hbcStatus '" + e.getMessage + "' does not exist.", e))
    case e: VerifyError =>
      (PreconditionFailed, ApiErrorModel.fromException(e))
    case e: ClassCastException =>
      (UnsupportedMediaType, ApiErrorModel.fromException(e))
    case e: IllegalArgumentException =>
      (BadRequest, ApiErrorModel.fromException(e))
    case e: JsResultException =>
      (BadRequest, ApiErrorModel.fromException(e))
    case e: TimeoutException =>
      (RequestTimeout, ApiErrorModel.fromException(e))
    case NonFatal(e) =>
      (InternalServerError, ApiErrorModel.fromExceptionAndMessage(
        "Yikes! An error has occurred: " + e.getMessage, e))
  }

  def handlerForRequest(req: RequestHeader): (Status, ApiErrorModel) => Result = {
    implicit val request = req
    (status, err) =>
      writeResponse(
        status,
        constructErrorResponseModel(Seq(err))
      )
  }
    
  def defaultExceptionHandler(req: RequestHeader): PartialFunction[Throwable, Result] =
    findResponseStatus andThen handlerForRequest(req).tupled
}

object ControllerPayloadLike extends ControllerPayload
