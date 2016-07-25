package plugins

import java.net.URL
import javax.inject.Inject

import play.api.{Application, Configuration, Environment}
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n.{DefaultMessagesApi, Lang, Langs, Messages}
import play.utils.Resources

import scala.collection.JavaConverters._

class HBCMessagesPlugin @Inject() (app: Application, config: Configuration, env: Environment, langs: Langs) extends DefaultMessagesApi(env, config, langs) {

  val GLOBAL_MESSAGES_PATH = "messages"
  val BANNER_CONF = "hbc.banner"

  override def isDefinedAt(key: String)(implicit lang: Lang): Boolean = true

  private lazy val bannerMessagesPath = joinPaths(Some(GLOBAL_MESSAGES_PATH), config.getString(BANNER_CONF).get)

  private def joinPaths(first: Option[String], second: String) = first match {
    case Some(first) => new java.io.File(first, second).getPath
    case None        => second
  }

  override def loadMessages(file: String): Map[String, String] = {
    val globalMessages: List[URL] = app.classloader.getResources(joinPaths(Some(GLOBAL_MESSAGES_PATH), file)).asScala.toList
    val bannerMessages: List[URL] = app.classloader.getResources(joinPaths(Some(bannerMessagesPath), file)).asScala.toList

    val cl = getClass.getClassLoader

    (bannerMessages ++ globalMessages).filterNot(Resources.isDirectory(cl, _)).reverse.map { messageFile =>
      Messages.parse(UrlMessageSource(messageFile), messageFile.toString).fold(e => throw e, identity)
    }.foldLeft(Map.empty[String, String]) { _ ++ _ }
  }
}
