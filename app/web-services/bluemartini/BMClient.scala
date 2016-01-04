package webservices.bluemartini

import com.s5a.hornetq_client.protocol.Request.JSONResponse
import models.{ ApiErrorModel, FailureResponse, SuccessfulResponse, ResponseData }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BMClient {
  def processBMResponse(jsonResponse: Future[JSONResponse]): Future[ResponseData] = {
    jsonResponse.map {
      jsResp =>
        (jsResp.body \ "results").validate[SuccessfulResponse]
          .getOrElse {
            (jsResp.body \ "results").validate[FailureResponse].getOrElse(FailureResponse(Seq(ApiErrorModel(s"Unable to read body response ${jsResp.body}", "JSON Error")), 500))
          }
    }
  }
}
