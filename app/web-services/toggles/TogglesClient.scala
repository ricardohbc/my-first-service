package webservices.toggles

import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent._
import scala.concurrent.duration._
import spray.caching._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import models._
import javax.inject.{Inject, Named}

import com.google.inject.ImplementedBy

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

@ImplementedBy(classOf[TogglesClient])
trait TogglesClientLike extends IndividualToggleCache with AllTogglesCache {
  def getToggle(name: String): Future[Option[Toggle]]
  def getToggleState(name: String): Future[Option[Boolean]]
  def getToggles(): Future[Seq[Toggle]]
  def clearCache(name: Option[String]): Unit
}

object TogglesClient {
  def apply(
    gt:  String => Future[Option[Toggle]]  = _ => Future.successful(None),
    gts: String => Future[Option[Boolean]] = _ => Future.successful(None),
    gtS: () => Future[Seq[Toggle]]         = () => Future.successful(Seq.empty[Toggle]),
    cc:  Option[String] => Unit            = _ => ()
  ): TogglesClientLike = new TogglesClientLike {
    def getToggle(name: String): Future[Option[Toggle]] = gt(name)
    def getToggleState(name: String): Future[Option[Boolean]] = gts(name)
    def getToggles(): Future[Seq[Toggle]] = gtS()
    def clearCache(name: Option[String]): Unit = cc(name)
  }
}

class TogglesClient @Inject() (@Named("togglesUrl") svcUrl: String, ws: WSClient) extends TogglesClientLike {

  // no idea about ttl just yet. Do they change much? The service is pretty snappy so could be less aggressive with the cache if useful
  // the dev toggle server returns about 260 toggles total right now
  //private val toggleCache: Cache[Toggle] = LruCache(maxCapacity = 500, initialCapacity = 275, timeToLive = Duration(24, "hours"))
  //private val allTogglesCache: Cache[Seq[Toggle]] = LruCache(maxCapacity = 1, initialCapacity = 1, timeToLive = Duration(24, "hours"))
  val unpackJsonResults: JsValue => JsValue = (json) => (json \ "response" \ "results").get

  private def getFromToggleSvc[T](reqUrl: String)(handler: JsValue => T): Future[Option[T]] =
    ws.url(reqUrl).get().map { response =>
      Logger.info("response status: " + response.status)
      Logger.info("body: " + response.body)

      if (response.status == 200)
        Some(handler(response.json))
      else if (response.status == 404)
        None
      else {
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
