package helpers

import com.typesafe.config.ConfigFactory
import scala.util.Try
import scala.collection.JavaConverters._

trait ConfigHelper {
  lazy val config = ConfigFactory.load()

  val configPrefix: Option[String] = None

  def getParam(param: String) = configPrefix.map {
    configPrefix =>
      s"$configPrefix.$param"
  }.getOrElse(param)

  def getStringProp(param: String): String = Try(config.getString(getParam(param))).getOrElse("")
  def getIntProp(param: String): Int = Try(config.getInt(getParam(param))).getOrElse(0)
  def getBooleanProp(param: String): Boolean = Try(config.getBoolean(getParam(param))).getOrElse(false)
  def getStringListProp(param: String): Seq[String] = Try(config.getStringList(getParam(param)).asScala).getOrElse(Seq())
}
