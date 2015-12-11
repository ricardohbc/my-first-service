package plugins

import java.net.URL

import play.api.Application
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n.{ Messages, DefaultMessagesPlugin }
import play.utils.Resources
import scala.collection.JavaConverters._

class HBCMessagesPlugin(app: Application) extends DefaultMessagesPlugin(app) {

  val GLOBAL_MESSAGES_PATH = "messages"
  val BANNER_JVM_PARAM = "hbc_banner"

  override def enabled = true

  private lazy val hbcBannerName = System.getProperty(BANNER_JVM_PARAM) match {
    case null => None
    case str  => Some(str)
  }

  private lazy val bannerMessagesPath = hbcBannerName.map { str => joinPaths(Some(GLOBAL_MESSAGES_PATH), str) }

  private def joinPaths(first: Option[String], second: String) = first match {
    case Some(first) => new java.io.File(first, second).getPath
    case None        => second
  }

  override def loadMessages(file: String): Map[String, String] = {
    val globalMessages: List[URL] = app.classloader.getResources(joinPaths(Some(GLOBAL_MESSAGES_PATH), file)).asScala.toList
    val bannerMessages: List[URL] = app.classloader.getResources(joinPaths(bannerMessagesPath, file)).asScala.toList

    (bannerMessages ++ globalMessages).filterNot(Resources.isDirectory).reverse.map { messageFile =>
      Messages.messages(UrlMessageSource(messageFile), messageFile.toString).fold(e => throw e, identity)
    }.foldLeft(Map.empty[String, String]) { _ ++ _ }
  }
}
