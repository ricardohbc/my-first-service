import play.api._
import play.api.mvc._
import play.api.GlobalSettings

object Global extends GlobalSettings {

  override def doFilter(next: EssentialAction): EssentialAction =
    Filters(super.doFilter(next), ServiceFilters.TimingFilter, ServiceFilters.IncrementFilter, ServiceFilters.TimeoutFilter)

}
