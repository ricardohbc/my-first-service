package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}

class FilterProvider(
    environment:   Environment,
    configuration: Configuration
) extends AbstractModule {
  def configure() = {
    bind(classOf[filters.TimingFilter])
    bind(classOf[filters.IncrementFilter])
    bind(classOf[filters.ExceptionFilter])
  }
}
