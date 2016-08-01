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
      .toInstance("/v1") //for backwards compatibility...for now

    bind(classOf[String])
      .annotatedWith(Names.named("banner"))
      .toInstance(configuration.getString("hbc.banner").get)
  }
}
