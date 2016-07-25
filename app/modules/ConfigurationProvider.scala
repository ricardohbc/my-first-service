package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.{Configuration, Environment}

class ConfigurationProvider(
    environment:   Environment,
    configuration: Configuration
) extends AbstractModule {
  def configure() = {
    bind(classOf[String])
      .annotatedWith(Names.named("versionURI"))
      .toInstance(configuration.getString("application.context").get)

    bind(classOf[String])
      .annotatedWith(Names.named("banner"))
      .toInstance(configuration.getString("hbc.banner").get)
  }
}
