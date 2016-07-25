
package filters

import javax.inject.Inject
import play.api.http.HttpFilters

// common logging and metrics for all requests
class ServiceFilters @Inject() (
    timing:    TimingFilter,
    increment: IncrementFilter,
    exception: ExceptionFilter
) extends HttpFilters {
  val filters = Seq(timing, increment, exception)
}
