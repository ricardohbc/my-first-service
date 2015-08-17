package utils

import helpers.ConfigHelper

object TestUtils extends ConfigHelper {
  val versionCtx = getStringProp("application.context")
}
