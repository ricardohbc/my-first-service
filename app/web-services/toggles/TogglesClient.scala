package webservices.toggles

import helpers.ConfigHelper
import play.api.Logger
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.duration._
import spray.caching._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models._

// no idea about ttl just yet. Do they change much? The service is pretty snappy so could be less aggressive with the cache if useful
// the dev toggle server returns about 260 toggles total right now
trait IndividualToggleCache {
  val toggleCache: Cache[Option[Toggle]] = LruCache(maxCapacity = 500, initialCapacity = 275, timeToLive = Duration(24, "hours"))
  def addToCache(toggle: Toggle) = toggleCache(toggle.toggle_name, () => Future.successful(Some(toggle)))
}

trait AllTogglesCache {
  val allTogglesCache: Cache[Seq[Toggle]] = LruCache(maxCapacity = 1, initialCapacity = 1, timeToLive = Duration(24, "hours"))
  def addToAllTogglesCache(key: String, toggles: Seq[Toggle]) = allTogglesCache(key, () => Future.successful(toggles))
}

object TogglesClient extends IndividualToggleCache with AllTogglesCache with ConfigHelper {

  private val svcUrl = config.getString("webservices.toggles.url")

  // no idea about ttl just yet. Do they change much? The service is pretty snappy so could be less aggressive with the cache if useful
  // the dev toggle server returns about 260 toggles total right now
  //private val toggleCache: Cache[Toggle] = LruCache(maxCapacity = 500, initialCapacity = 275, timeToLive = Duration(24, "hours"))
  //private val allTogglesCache: Cache[Seq[Toggle]] = LruCache(maxCapacity = 1, initialCapacity = 1, timeToLive = Duration(24, "hours"))
  val unpackJsonResults: JsValue => JsValue = (json) => (json \ "response" \ "results")

  private def getFromToggleSvc[T](reqUrl: String)(handler: JsValue => T): Future[Option[T]] =
    WS.url(reqUrl).get().map { response =>
      Logger.info("response status: " + response.status)
      Logger.info("body: " + response.body)

      if (response.status == 200) {
        try {
          Some(handler(response.json))
        } catch {
          case e: Exception => {
            Logger.error(s"Error while parsing toggle-service response (toggle doesn't exist?): '${reqUrl}', defaulting toggle state to false: ", e)
            None
          }
        }
      } else if (response.status == 404) {
        Logger.error(s"HTTP 404 status received from toggle-service: '${reqUrl}', defaulting toggle state to false.")
        None
      } else {
        val msg = "toggle web request failed with: " + response.body
        Logger.info(msg)
        throw new Exception(msg)
      }
    }

  private def getCachedToggle(name: String): Future[Option[Toggle]] = toggleCache(name) {
    getFromToggleSvc(s"$svcUrl/$name") { js => unpackJsonResults(js).as[Toggle] }
  }

  private def getAllToggles(): Future[Seq[Toggle]] =
    getFromToggleSvc(svcUrl) { js => unpackJsonResults(js).as[Seq[Toggle]] }.map {
      case Some(toggles) =>
        toggles.foreach(addToCache) // shove them in the individual toggle cache
        toggles.sortBy(_.toggle_name)
      case _ => Seq.empty[Toggle]
    }

  private def getCachedToggles(): Future[Seq[Toggle]] = allTogglesCache("all") {
    getAllToggles()
  }

  private def clearBothCaches(): Unit = {
    toggleCache.clear
    allTogglesCache.clear
  }

  // ************** Exposed Services ****************************
  def getToggle(name: String): Future[Option[Toggle]] = getCachedToggle(name)

  def getToggleState(name: String): Future[Option[Boolean]] = getCachedToggle(name).map(_.map(_.toggle_state))

  def getToggles(): Future[Seq[Toggle]] = getCachedToggles()

  def clearCache(name: Option[String]) =
    name.fold(clearBothCaches)(k => toggleCache.remove(k))

}

