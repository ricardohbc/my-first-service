package helpers

import constants.Constants
import models._
import play.api.libs.json._
import play.api.mvc._

import scala.util.{Try, Success}
import scala.concurrent._
import play.api.libs.json.JsSuccess
import scala.util.Failure
import play.api.mvc.Result
import models.ApiResponseModel
import scala.util.Success
import models.ApiErrorMessageModel
import play.api.libs.json.JsResultException

trait ControllerPayload extends Controller
  with ApiResponse
  with ApiError {

  val actorSystemName = "hbcStatus"

  ////////////////////////
  //      RESPONSE      //
  ////////////////////////

  def writeResponseStore[T : Writes](request: Request[AnyContent], result: Try[T]): Result =
    writeResponse(request, result, Created)

  def writeResponseStores[T : Writes](request: Request[AnyContent], results: Try[Seq[Try[T]]]): Result =
    writeResponses(request, results, Created)

  def writeResponseGet[T : Writes](request: Request[AnyContent], result: Try[T]): Result =
    writeResponse(request, result, Ok)

  def writeResponseGet1[T : Writes](request: Request[AnyContent], result: T): Result =
    writeResponse(request, Success(result), Ok)

  def writeResponseUpdate[T : Writes](request: Request[AnyContent], result: Try[T]): Result =
    writeResponse(request, result, Ok)

  def writeResponseUpdates[T : Writes](request: Request[AnyContent], results: Try[Seq[Try[T]]]): Result =
    writeResponses(request, results, Ok)

  def writeResponseRemove[T : Writes](request: Request[AnyContent], result: Try[T]): Result =
    writeResponse(request, result, Ok)

  private def writeResponse[T : Writes](request: Request[AnyContent], result: Try[T], responseCode: Status): Result = {
    var response: Status = responseCode
    var status: String = Constants.STATUS_COMPLETE
    var errs: Seq[ApiErrorMessageModel] = Seq[ApiErrorMessageModel]()

    if (result.isFailure) {
      status = Constants.STATUS_ERROR
      val (resp, err) = getError(result.failed.get)
      response = resp
      errs = errs :+ err
    }

    val apiResponse = ApiResponseModel.apply(
      Json.toJson(ApiRequestModel(request)),
      Json.obj(
        Constants.RESPONSE_STATUS -> status,
        Constants.RESULTS -> result.toOption
      ),
      Json.toJson(errs)
    )

    response.apply(Json.prettyPrint(Json.toJson(apiResponse))).as(JSON)
  }

  private def writeResponses[T : Writes](request: Request[AnyContent], results: Try[Seq[Try[T]]], responseCode: Status): Result = {
    var output = Seq[Option[T]]()
    var response: Status = responseCode
    var status: String = Constants.STATUS_COMPLETE
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
      status = Constants.STATUS_ERROR
    }

    val apiResponse = ApiResponseModel.apply(
      Json.toJson(ApiRequestModel(request)),
      Json.obj(
        Constants.RESPONSE_STATUS -> status,
        Constants.RESULTS -> output
      ),
      Json.toJson(errs)
    )
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

  def onHandlerRequestTimeout(request: Request[AnyContent]): Result =
    writeResponse(request, Failure[String](new TimeoutException(Constants.TIMEOUT_MSG)), RequestTimeout)

  private def getRequestBodyAsJson(request: Request[AnyContent]): JsValue = {
    //Check if valid JSON payload was received
    val o = request.body.asJson
    if (o.isEmpty) {
      throw new IllegalArgumentException()
    }

    o.get
  }

  def getError(err: Throwable): (Status, ApiErrorMessageModel) = {
    err match {
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
}

object ControllerPayloadLike extends ControllerPayload
