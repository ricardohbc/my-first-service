import scala.concurrent.Future
import scala.util.control.NonFatal
import models._
import play.api._
import play.api.mvc._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.GlobalSettings
import helpers.ControllerPayloadLike
import constants._

object Global extends GlobalSettings with ApiResponse with ApiError {

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(super.doFilter(next), ServiceFilters.TimingFilter, ServiceFilters.IncrementFilter)

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {

    val (status, errModel) = ControllerPayloadLike.getError(ex)

    val none: Option[String] = None

    val apiResponse = ApiResponseModel.apply(
      Json.toJson(ApiRequestModel(request)),
      Json.obj(
        Constants.RESPONSE_STATUS -> Constants.STATUS_ERROR,
        Constants.RESULTS -> none
      ),
      Json.toJson(Seq(errModel))
    ) 

    Future.successful(status.apply(Json.prettyPrint(Json.toJson(apiResponse))))
  }

}
