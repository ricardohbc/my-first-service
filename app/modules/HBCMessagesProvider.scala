package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import play.api.i18n.{DefaultLangs, Langs, MessagesApi}
import play.api.{Configuration, Environment}

class HBCMessagesProvider(
    environment:   Environment,
    configuration: Configuration
) extends AbstractModule {
  def configure() = {
    bind(classOf[Langs]).to(classOf[DefaultLangs])
    bind(classOf[MessagesApi]).to(classOf[plugins.HBCMessagesPlugin])
    bind(classOf[String])
      .annotatedWith(Names.named("hbc.messages.path"))
      .toInstance(configuration.getString("hbc.messages.path").get)
  }
}
