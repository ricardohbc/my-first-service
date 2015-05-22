package helpers

import com.typesafe.config.ConfigFactory

trait ConfigHelper {
  lazy val config = ConfigFactory.load()
}
