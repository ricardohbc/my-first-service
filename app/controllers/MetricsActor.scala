package controllers

//////////////////////////////////////
//This is a temporary class, as we iterate over metrics jar. eventually this will be moved into the new scala metrics jar.
/////////////////////////////////////

import akka.actor.{ActorSystem, ActorRef, Props, Actor}
import com.s5a.metrics.{Recorder, MetricNamespace}
import play.api.Logger

////////////////////////
//      MESSAGES      //
////////////////////////

case class TimeMessage(tag: String, ms: Long)
case class IncrementMessage(tag: String)
case class GaugeMessage(tag: String, num: Long)

////////////////////////
//       ACTOR        //
////////////////////////

object MetricActor {
  def props(namespace: MetricNamespace): Props = Props(classOf[MetricActor], namespace)
}

class MetricActor(namespace: MetricNamespace) extends Actor {
  val actorSystemName = context.system.name

  def receive = {
    case TimeMessage(tag, ms) => Recorder.time(namespace, tag, ms)
    case IncrementMessage(tag) => Recorder.increment(namespace, tag)
    case GaugeMessage(tag, num) => Recorder.gauge(namespace, tag, num)
  }

  override def unhandled(msg: Any): Unit = {
    Logger.debug(s"Unhandled message in metric actor. System '$actorSystemName', Namespace: '$namespace', Message: '$msg'")
    super.unhandled(msg)
  }
}

////////////////////////
//       TRAITS       //
////////////////////////

trait Timer{
  def time(tag: String, ms: Long)
  def time [A](tag:String)(f: => A):A
}

trait Counter{
  def increment(tag: String)
}

trait Gauger{
  def gauge(tag: String, num: Long)
}

//Used by recorder classes
trait RecorderLike extends Timer with Counter with Gauger

//Used by other classes to mix in recorder functionality
trait RecorderTrait extends RecorderLike with StatsDClient {
  def recorder: RecorderLike
  def time(tag: String, ms: Long) = recorder.time(tag, ms)
  def time [A](tag:String)(f: => A):A = recorder.time(tag){f}
  def increment(tag: String) = recorder.increment(tag)
  def gauge(tag: String, num: Long) = recorder.gauge(tag, num)
}

////////////////////////
//     RECORDERS      //
////////////////////////

object MetricRecorder{
  def apply(system: ActorSystem, namespace: MetricNamespace): MetricRecorder = {
    val metrics = system.actorOf(MetricActor.props(namespace), name = "metrics")
    new MetricRecorder(metrics)
  }
}

class MetricRecorder(metric: ActorRef) extends RecorderLike {
  def time(tag: String, ms: Long) = metric ! TimeMessage(tag, ms)
  def time [A](tag:String)(f: => A):A = {
    val start = System.currentTimeMillis
    val ret = f
    val end = System.currentTimeMillis
    time(tag, end - start)
    ret
  }
  def increment(tag: String) = metric ! IncrementMessage(tag)
  def gauge(tag: String, num: Long) = metric ! GaugeMessage(tag, num)
}

object NullMetricRecorder{
  def apply(): NullMetricRecorder = {
    new NullMetricRecorder()
  }
}

class NullMetricRecorder() extends RecorderLike {
  def time(tag: String, ms: Long){/*Do Nothing*/}
  def time [A](tag:String)(f: => A):A = f
  def increment(tag: String){/*Do Nothing*/}
  def gauge(tag: String, num: Long){/*Do Nothing*/}
}
