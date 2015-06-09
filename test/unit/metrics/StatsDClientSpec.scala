package unit.helpers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.scalatest.prop.PropertyChecks
import metrics.{StatsDClient, StatsDProtocol}

class StatsDClientSpec extends WordSpec
with Matchers
with PropertyChecks
with StatsDClient {

  "StatsDProtocol" should {
    "format a string according to the StatsD protocol" in {
      forAll("key", "value", "metric", "sampleRate") { (key: String, value: String, metric: String, sampleRate: Double) =>
        val stat = key + ":" + value + "|" + metric + (if (sampleRate < 1) "|@" + sampleRate  else "")
        StatsDProtocol.stat(key, value, metric, sampleRate) should equal (stat)
      }
    }

    "time a future" in {
      timeTaken(0, Future { Thread.sleep(50); 6 }).map { time =>
        assert(time >= 50)
      }
    }

    "time something synchronous" in {
      timeTaken(0, { val a = 10; a }).map { time =>
        assert(time < 50)
      }
    }
  }
}