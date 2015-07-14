package helpers

import com.typesafe.config.ConfigFactory

trait ConfigHelper {
  lazy val config = ConfigFactory.load()

  val configPrefix: Option[String] = None

  def getParam(param: String) = configPrefix.map {
    configPrefix =>
      s"$configPrefix.$param"
  }.getOrElse(param)

  def getStringProp(param: String) = config.getString(getParam(param))
  def getIntProp(param: String) = config.getInt(getParam(param))
  def getBooleanProp(param: String) = config.getBoolean(getParam(param))
  def getStringListProp(param: String) = config.getStringList(getParam(param))
}