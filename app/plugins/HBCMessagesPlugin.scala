package plugins

import java.net.URL
import javax.inject.{Inject, Named}

import play.api.{Configuration, Environment}
import play.api.i18n.Messages.UrlMessageSource
import play.api.i18n.{DefaultMessagesApi, Lang, Langs, Messages}
import play.utils.Resources

class HBCMessagesPlugin @Inject() (env: Environment, config: Configuration, langs: Langs, @Named("hbc.messages.path") hbcMessagesPath: String, @Named("banner") banner: String) extends DefaultMessagesApi(env, config, langs) {

  override def isDefinedAt(key: String)(implicit lang: Lang): Boolean = true

  private def joinPaths(first: Option[String], second: String) = first.map { f =>
    new java.io.File(f, second).getPath
  }.getOrElse(second)

  override def loadMessages(file: String): Map[String, String] = {
    val bannerMessagesPath = joinPaths(Some(hbcMessagesPath), banner)

    val globalMessages: List[URL] = env.resource(joinPaths(Some(hbcMessagesPath), file)).toList
    val bannerMessages: List[URL] = env.resource(joinPaths(Some(bannerMessagesPath), file)).toList

    (bannerMessages ++ globalMessages).filterNot(Resources.isDirectory(env.classLoader, _)).reverse.map { messageFile =>
      Messages.parse(UrlMessageSource(messageFile), messageFile.toString).fold(e => throw e, identity)
    }.foldLeft(Map.empty[String, String]) { _ ++ _ }
  }
}
