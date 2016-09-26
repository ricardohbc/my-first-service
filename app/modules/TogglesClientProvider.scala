package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.{Configuration, Environment}

/**
 * Created by anthonygaro on 12/4/15.
 */
class TogglesClientProvider(
    environment:   Environment,
    configuration: Configuration
) extends AbstractModule {
  def configure() = {
    bind(classOf[String])
      .annotatedWith(Names.named("togglesUrl"))
      .toInstance(configuration.getString("webservices.toggles.url").get)
  }
}

