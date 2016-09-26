package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.{Configuration, Environment}

class ControllerTimeoutProvider(
    environment:   Environment,
    configuration: Configuration
) extends AbstractModule {
  def configure() = {
    val to = configuration.getInt("controllers.timeout").get
    bind(classOf[Integer]).annotatedWith(Names.named("controllerTimeout")).toInstance(to)
    bind(classOf[helpers.ControllerTimeout])
  }
}
