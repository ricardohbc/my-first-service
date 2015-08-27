package utils

import globals.GlobalServiceSettings
import helpers.ConfigHelper
import play.api.Application

object TestUtils extends ConfigHelper {
  val versionCtx = getStringProp("application.context")
}

object TestGlobal extends GlobalServiceSettings {
  override def onStart(app: Application): Unit = {

  }
}
