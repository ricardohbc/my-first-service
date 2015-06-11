package helpers

import constants.Constants
import models._
import play.api.libs.json._
import play.api.mvc._

import scala.util.{Try, Success}
import scala.concurrent._
import play.api.libs.json.JsSuccess
import scala.util.Failure
import scala.util.control.NonFatal
import play.api.mvc.Result
import models.ApiResponseModel
import scala.util.Success
import models.ApiErrorMessageModel
import play.api.libs.json.JsResultException
import play.Logger

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
    writeResponse(responseStatus, constructResponseModel(request, result, Constants.COMPLETE_MESSAGE))

  def writeResponseFailure(ex: Throwable)(implicit request: RequestHeader): Result = {
    val nothing: Option[String] = None // seems to forestall a weird implicit conflict on the api json models.!!
    val (responseStatus, err) = getError(ex)
    val body = constructResponseModel(request, nothing, Constants.ERROR_MESSAGE, Seq(err))
    writeResponse(responseStatus, body)
  }
  private def writeResponse(responseStatus: Status, body: ApiResponseModel) =
    responseStatus.apply(Json.prettyPrint(Json.toJson(body))).as(JSON)

  def constructResponseModel[T: Writes](
    req: RequestHeader,
    result: T,
    message: String,
    errs: Seq[ApiErrorMessageModel] = Seq()): ApiResponseModel =
      ApiResponseModel.apply(
        Json.toJson(ApiRequestModel(req)),
        Json.obj(
          Constants.RESPONSE_MESSAGE -> message,
          Constants.RESULTS -> result
        ),
        Json.toJson(errs)
      )

  private def writeResponses[T : Writes](results: Try[Seq[Try[T]]], responseCode: Status)(implicit request: Request[AnyContent]): Result = {
    var output = Seq[Option[T]]()
    var response: Status = responseCode
    var message: String = Constants.COMPLETE_MESSAGE
    var errs: Seq[ApiErrorMessageModel] = Seq[ApiErrorMessageModel]()

    results match {
      case Failure(e) =>
        val (resp, err) = getError(results.failed.get)
        response = resp
        errs = errs :+ err
      case Success(seq) =>
        errs = errs ++ seq.filter(_.isFailure).map(f => {
          val (resp, err) = getError(f.failed.get)
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

    val apiResponse = constructResponseModel(request, output, message, errs)

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

  def onHandlerRequestTimeout(request: RequestHeader): Result = {
    writeResponseFailure(new TimeoutException(Constants.TIMEOUT_MSG))(request)
  }

  private def getRequestBodyAsJson(request: Request[AnyContent]): JsValue = {
    //Check if valid JSON payload was received
    val o = request.body.asJson
    if (o.isEmpty) {
      throw new IllegalArgumentException()
    }

    o.get
  }

  private def getError(err: Throwable): (Status, ApiErrorMessageModel) = err match {
    case e: NoSuchElementException =>
      (NotFound, ApiErrorMessageModel.apply(
        "hbcStatus '" + e.getMessage + "' does not exist.",
        e.getClass.getSimpleName
      ))
    case e: VerifyError =>
      (PreconditionFailed, ApiErrorMessageModel.apply(
        e.getMessage,
        e.getClass.getSimpleName
      ))
    case e: ClassCastException =>
      (UnsupportedMediaType, ApiErrorMessageModel.apply(
        e.getMessage,
        e.getClass.getSimpleName
      ))
    case e: JsResultException =>
      (BadRequest, ApiErrorMessageModel.apply(
        e.getMessage,
        e.getClass.getSimpleName
      ))
    case e: TimeoutException =>
      (RequestTimeout, ApiErrorMessageModel.apply(
        e.getMessage,
        e.getClass.getSimpleName
      ))
    case e: Throwable =>
      (InternalServerError, ApiErrorMessageModel.apply(
        "Yikes! An error has occurred: " + e.getMessage,
        e.getClass.getSimpleName
      ))
  }
}

object ControllerPayloadLike extends ControllerPayload
