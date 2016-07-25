package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}

class StatsDProvider(
    environment:   Environment,
    configuration: Configuration
) extends AbstractModule {
  def configure() = {
    val recorder = new metrics.StatsDClient(
      host = configuration.getString("statsd.metric-host").get,
      port = configuration.getInt("statsd.port").get,
      server = configuration.getString("statsd.server").get,
      namespace = configuration.getString("statsd.metric-namespace").get
    )
    bind(classOf[metrics.StatsDClientLike]).toInstance(recorder)
  }
}
