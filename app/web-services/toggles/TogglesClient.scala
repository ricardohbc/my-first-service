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

object TogglesClient extends ConfigHelper {

  private val svcUrl = config.getString("webservices.toggles.url")

  // no idea about ttl just yet
  // the dev toggle server returns about 260 toggles total right now
  private val toggleCache: Cache[Toggle] = LruCache(maxCapacity = 500, initialCapacity = 275, timeToLive = Duration(30, "minutes"))
  private val allTogglesCache: Cache[Seq[Toggle]] = LruCache(maxCapacity = 1, initialCapacity = 1, timeToLive = Duration(10, "minutes"))

  private def getCachedToggle(name: String): Future[Toggle] = toggleCache(name) {
    getFromToggleSvc(s"$svcUrl/$name") { json => (json \ "response" \ "results").as[Toggle] }
  }

  private def getFromToggleSvc[T](reqUrl: String)(handler: JsValue => T): Future[T] =
    WS.url(reqUrl).get().map { response =>
      if (response.status == 200)
        handler(response.json)
      else {
        val msg = "toggle web request failed with: " + response.body
        Logger.info(msg)
        throw new Exception(msg)
      }
    }

  private def getAllToggles(): Future[Seq[Toggle]] =
    getFromToggleSvc(svcUrl) { json => (json \ "response" \ "results").as[Seq[Toggle]] }.map { toggles =>
      // shove them in the individual toggle cache
      toggles.foreach { toggle =>
        toggleCache(toggle.toggle_name, { () => Future.successful(toggle) })
      }
      toggles.sortBy(_.toggle_name)
    }

  private def getCachedToggles(): Future[Seq[Toggle]] = allTogglesCache("all") {
    getAllToggles()
  }

  def getToggle(name: String): Future[Toggle] = getCachedToggle(name)

  def getToggles(): Future[Seq[Toggle]] = getCachedToggles()
}

