package helpers

import constants.Constants
import models._
import play.api.libs.json._
import play.api.mvc._

import scala.util.Try
import scala.concurrent._
import play.api.libs.json.JsSuccess
import scala.util.Failure
import scala.util.control.NonFatal
import play.api.mvc.Result
import scala.util.Success
import play.api.libs.json.JsResultException

trait ControllerPayload extends Controller {

  ////////////////////////
  //      RESPONSE      //
  ////////////////////////

  def writeResponseStore[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Created)

  def writeResponseStores[T : Format](results: Try[Seq[Try[T]]])(implicit request: Request[AnyContent]): Result =
    writeResponses(results, Created)

  def writeResponseGet[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseGet(response: ApiResultModel, errors: Seq[ApiErrorModel])(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(response, errors, Ok)

  def writeResponseUpdate[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseUpdates[T : Format](results: Try[Seq[Try[T]]])(implicit request: Request[AnyContent]): Result =
    writeResponses(results, Ok)

  def writeResponseRemove[T : Format](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseSuccess[T : Format](result: T, responseStatus: Status)(implicit request: RequestHeader): Result =
    writeResponse(responseStatus, constructResponseModel(constructResultModel(result)))

  def writeResponseSuccess(result: ApiResultModel, errors: Seq[ApiErrorModel], responseStatus: Status)(implicit request: RequestHeader): Result =
    writeResponse(responseStatus, constructResponseModel(result, errors))

  def writeResponseError(ex: Throwable)(implicit request: RequestHeader): Result =
    defaultExceptionHandler(request)(ex)

  def writeResponse(responseStatus: Status, body: ApiModel): Result =
    responseStatus.apply(Json.prettyPrint(Json.toJson(body))).as(JSON)

  def constructResultModel[T: Format](result: T): ApiResultModel = {
    ApiResultModel(Json.toJson(result))
  }

  def constructResponseModel(
    result: ApiResultModel = ApiResultModel(JsNull),
    errs: Seq[ApiErrorModel] = Seq())(implicit request: RequestHeader): ApiModel =
      ApiModel.apply(
        ApiRequestModel.fromReq(request),
        result,
        errs
      )

  private def writeResponses[T : Format](results: Try[Seq[Try[T]]], responseCode: Status)(implicit request: Request[AnyContent]): Result = {
    var output = Seq[T]()
    var response: Status = responseCode
    var errs: Seq[ApiErrorModel] = Seq[ApiErrorModel]()
    val errorFunction =
      results match {
        case Failure(e) =>
          val (resp, err) = findResponseStatus(results.failed.get)
          response = resp
          errs = errs :+ err
        case Success(seq) =>
          errs = errs ++ seq.filter(_.isFailure).map(f => {
            val (resp, err) = findResponseStatus(f.failed.get)
            if (response.header.status < resp.header.status){
              response = resp
            }
            err
          })
          output = seq.flatMap(_.toOption.toList)
      }

    val apiResponse = constructResponseModel(constructResultModel(output), errs)

    response.apply(Json.prettyPrint(Json.toJson(apiResponse))).as(JSON)
  }

  ////////////////////////
  //     GET ITEMS      //
  ////////////////////////

  def getRequestItem[T: Reads](request: Request[AnyContent]): Try[T] = Try {
    val readJsonObject: Reads[JsValue] = (__ \ "item").read[JsValue]
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

  def getRequestItems[T: Reads](request: Request[AnyContent]): Try[Seq[Try[T]]] = Try {
    val readJsonObject: Reads[Seq[JsValue]] = (__ \ "items").read[Seq[JsValue]]
    getRequestBodyAsJson(request).validate(readJsonObject) match {
      case JsError(e) => throw new JsResultException(e)
      case JsSuccess(hbcStatusObjectList, _) =>
        hbcStatusObjectList.map(hbcStatusObject => Try {
          hbcStatusObject.validate[T] match {
            case JsSuccess(hbcStatus, _) => hbcStatus
            case JsError(e) => throw new ClassCastException("Could not cast '" + hbcStatusObject + "' into proper type")
          }
        })
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
        constructResponseModel(errs = Seq(err))
      )
  }
    
  def defaultExceptionHandler(req: RequestHeader): PartialFunction[Throwable, Result] =
    findResponseStatus andThen handlerForRequest(req).tupled

}

object ControllerPayloadLike extends ControllerPayload
