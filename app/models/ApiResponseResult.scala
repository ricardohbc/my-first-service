// package models

// import play.api.libs.json._

// case class ApiResponseResultModel[T : Format](message: String, result: T = JsNull) //(implicit val tWrites: Writes[T])

// object ApiResponseResultModel {
//   implicit val respFormat = Json.F
//   implicit def pageWriter[T]: Writes[ApiResponseResultModel[T]] = new Writes[ApiResponseResultModel[T]] {
//     def writes(o: ApiResponseResultModel[T]): JsValue = {
//       implicit val tWrites = o.tWrites
//       Json.obj(
//         "message" -> o.message,
//         "results" -> o.result
//       )
//     }
//   }
// }
