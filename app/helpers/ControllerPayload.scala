package helpers

import constants.Constants
import models._
import play.api.libs.json._
import play.api.mvc._

import scala.util.Try
import scala.concurrent._
import play.api.libs.json.JsSuccess
import scala.util.Failure
import play.api.mvc.Result
import scala.util.Success
import play.api.libs.json.JsResultException

trait ControllerPayload extends Controller {

  ////////////////////////
  //      RESPONSE      //
  ////////////////////////

  def writeResponseStore[T : Writes](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Created)

  def writeResponseStores[T : Writes](results: Try[Seq[Try[T]]])(implicit request: Request[AnyContent]): Result =
    writeResponses(results, Created)

  def writeResponseGet[T : Writes](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseUpdate[T : Writes](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseUpdates[T : Writes](results: Try[Seq[Try[T]]])(implicit request: Request[AnyContent]): Result =
    writeResponses(results, Ok)

  def writeResponseRemove[T : Writes](result: T)(implicit request: Request[AnyContent]): Result =
    writeResponseSuccess(result, Ok)

  def writeResponseSuccess[T : Writes](result: T, responseStatus: Status)(implicit request: RequestHeader): Result =
    writeResponse(responseStatus, constructResponseModel(request, ApiResponseResultModel[T](Constants.COMPLETE_MESSAGE, result)))

  private def writeResponse(responseStatus: Status, body: ApiResponseModel) =
    responseStatus.apply(Json.prettyPrint(Json.toJson(body))).as(JSON)

  def constructResponseModel[T: Writes](
    req: RequestHeader,
    result: ApiResponseResultModel[T],
    errs: Seq[ApiErrorMessageModel] = Seq()): ApiResponseModel =
      ApiResponseModel.apply(
        Json.toJson(ApiRequestModel(req)),
        Json.toJson(result),
        Json.toJson(errs)
      )

  private def writeResponses[T : Writes](results: Try[Seq[Try[T]]], responseCode: Status)(implicit request: Request[AnyContent]): Result = {
    var output = Seq[Option[T]]()
    var response: Status = responseCode
    var message: String = Constants.COMPLETE_MESSAGE
    var errs: Seq[ApiErrorMessageModel] = Seq[ApiErrorMessageModel]()
    val errorFunction =

    results match {
      case Failure(e) =>
        val (resp, err) = findResponseHandler(results.failed.get)
        response = resp
        errs = errs :+ err
      case Success(seq) =>
        errs = errs ++ seq.filter(_.isFailure).map(f => {
          val (resp, err) = findResponseHandler(f.failed.get)
          if (response.header.status < resp.header.status){
            response = resp
          }
          err
        })
        output = seq.map(_.toOption)
    }

    if (!errs.isEmpty){
      message = Constants.ERROR_MESSAGE
    }

    val apiResponse = constructResponseModel(request, ApiResponseResultModel(message, output), errs)

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

  def onHandlerRequestTimeout(request: RequestHeader): Result =
    responseExec(findResponseHandler(new TimeoutException(Constants.TIMEOUT_MSG)))(request)

  private def getRequestBodyAsJson(request: Request[AnyContent]): JsValue =
    request.body.asJson.fold(throw new IllegalArgumentException("no json found"))(x => x)

  def findResponseHandler: PartialFunction[Throwable, (Status, ApiErrorMessageModel)] = {
    case e: NoSuchElementException =>
      (NotFound, ApiErrorMessageModel(
        "hbcStatus '" + e.getMessage + "' does not exist.", e))
    case e: VerifyError =>
      (PreconditionFailed, ApiErrorMessageModel(e))
    case e: ClassCastException =>
      (UnsupportedMediaType, ApiErrorMessageModel(e))
    case e: IllegalArgumentException =>
      (BadRequest, ApiErrorMessageModel(e))
    case e: JsResultException =>
      (BadRequest, ApiErrorMessageModel(e))
    case e: TimeoutException =>
      (RequestTimeout, ApiErrorMessageModel(e))
    case e: Throwable =>
      (InternalServerError, ApiErrorMessageModel(
        "Yikes! An error has occurred: " + e.getMessage, e))
  }

  def responseExec (handlerInfo: (Status, ApiErrorMessageModel))(request: RequestHeader) = {
    handlerInfo match {
      case (status, err) =>
        val body = constructResponseModel(request, ApiResponseResultModel(Constants.ERROR_MESSAGE), Seq(err))
        writeResponse(status, body)
    }
  }
}

object ControllerPayloadLike extends ControllerPayload
