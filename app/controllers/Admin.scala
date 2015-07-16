package controllers

import play.api._

import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.lang.management._
import scala.collection.mutable
import helpers.ControllerPayload
import commands.DemoCommand
import java.util.concurrent

object Admin extends Controller
  with ControllerPayload {

  @no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET /hbc-microservice-template/admin/ping

    DESCRIPTION
      Basic health check

    RESULT
      Response

  """)
  def ping = Action { implicit request =>
    Logger.debug("ping")
    writeResponseGet("pong")
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET /hbc-microservice-template/admin/jvmstats

    DESCRIPTION
      JVM statistics for the service

    RESULT
      JvmStats

    JvmStats: !
      jvm_num_cpus: Double
      jvm_current_mem_Code Cache_max: Double
      jvm_post_gc_PS Old Gen_max: Double
      jvm_post_gc_PS Eden Space_used: Double
      jvm_fd_limit: Double
      jvm_current_mem_PS Survivor Space_used: Double
      jvm_thread_count: Double
      jvm_post_gc_PS Perm Gen_used: Double
      jvm_classes_total_unloaded: Double
      jvm_current_mem_PS Eden Space_max: Double
      jvm_current_mem_PS Old Gen_max: Double
      jvm_nonheap_max: Double
      jvm_buffer_direct_count: Double
      jvm_buffer_mapped_used: Double
      jvm_buffer_direct_max: Double
      jvm_current_mem_PS Old Gen_used: Double
      jvm_classes_current_loaded: Double
      jvm_start_time: Double
      jvm_current_mem_Code Cache_used: Double
      jvm_buffer_mapped_count: Double
      jvm_post_gc_PS Survivor Space_max: Double
      jvm_buffer_direct_used: Double
      jvm_thread_peak_count: Double
      jvm_current_mem_used: Double
      jvm_post_gc_PS Eden Space_max: Double
      jvm_post_gc_used: Double
      jvm_current_mem_PS Perm Gen_used: Double
      jvm_uptime: Double
      jvm_heap_committed: Double
      jvm_compilation_time_msec: Double
      jvm_current_mem_PS Eden Space_used: Double
      jvm_classes_total_loaded: Double
      jvm_current_mem_PS Survivor Space_max: Double
      jvm_buffer_mapped_max: Double
      jvm_thread_daemon_count: Double
      jvm_heap_max: Double
      jvm_post_gc_PS Old Gen_used: Double
      jvm_fd_count: Double
      jvm_post_gc_PS Perm Gen_max: Double
      jvm_heap_used: Double
      jvm_post_gc_PS Survivor Space_used: Double
      jvm_nonheap_used: Double
      jvm_current_mem_PS Perm Gen_max: Double
      jvm_nonheap_committed: Double
  """)
  def jvmstats = Action.async { request =>

    Logger.debug("jvmstats")
    Future.successful(Ok(Json.prettyPrint(Json.toJson(extractJvmStats()))))
  }

  private def extractJvmStats(): JsValue = {
    import scala.collection.JavaConverters._

    val out: mutable.Map[String, Double] = mutable.Map.empty

    val mem = ManagementFactory.getMemoryMXBean

    val heap = mem.getHeapMemoryUsage
    out += ("jvm_heap_committed" -> heap.getCommitted)
    out += ("jvm_heap_max" -> heap.getMax)
    out += ("jvm_heap_used" -> heap.getUsed)

    val nonheap = mem.getNonHeapMemoryUsage
    out += ("jvm_nonheap_committed" -> nonheap.getCommitted)
    out += ("jvm_nonheap_max" -> nonheap.getMax)
    out += ("jvm_nonheap_used" -> nonheap.getUsed)

    val threads = ManagementFactory.getThreadMXBean
    out += ("jvm_thread_daemon_count" -> threads.getDaemonThreadCount.toLong)
    out += ("jvm_thread_count" -> threads.getThreadCount.toLong)
    out += ("jvm_thread_peak_count" -> threads.getPeakThreadCount.toLong)

    val runtime = ManagementFactory.getRuntimeMXBean
    out += ("jvm_start_time" -> runtime.getStartTime)
    out += ("jvm_uptime" -> runtime.getUptime)

    val os = ManagementFactory.getOperatingSystemMXBean
    out += ("jvm_num_cpus" -> os.getAvailableProcessors.toLong)
    os match {
      case unix: com.sun.management.UnixOperatingSystemMXBean =>
        out += ("jvm_fd_count" -> unix.getOpenFileDescriptorCount)
        out += ("jvm_fd_limit" -> unix.getMaxFileDescriptorCount)
      case _ => // ew, Windows... or something
    }

    val compilation = ManagementFactory.getCompilationMXBean
    out += ("jvm_compilation_time_msec" -> compilation.getTotalCompilationTime)

    val classes = ManagementFactory.getClassLoadingMXBean
    out += ("jvm_classes_total_loaded" -> classes.getTotalLoadedClassCount)
    out += ("jvm_classes_total_unloaded" -> classes.getUnloadedClassCount)
    out += ("jvm_classes_current_loaded" -> classes.getLoadedClassCount.toLong)

    var postGCTotalUsage = 0L
    var currentTotalUsage = 0L
    ManagementFactory.getMemoryPoolMXBeans.asScala.foreach { pool =>
      val name = pool.getName
      Option(pool.getCollectionUsage).foreach { usage =>
        out += ("jvm_post_gc_" + name + "_used" -> usage.getUsed)
        postGCTotalUsage += usage.getUsed
        out += ("jvm_post_gc_" + name + "_max" -> usage.getMax)
      }
      Option(pool.getUsage) foreach { usage =>
        out += ("jvm_current_mem_" + name + "_used" -> usage.getUsed)
        currentTotalUsage += usage.getUsed
        out += ("jvm_current_mem_" + name + "_max" -> usage.getMax)
      }
    }
    out += ("jvm_post_gc_used" -> postGCTotalUsage)
    out += ("jvm_current_mem_used" -> currentTotalUsage)

    ManagementFactory.getPlatformMXBeans(classOf[BufferPoolMXBean]).asScala.foreach { bp =>
      val name = bp.getName
      out += ("jvm_buffer_" + name + "_count" -> bp.getCount)
      out += ("jvm_buffer_" + name + "_used" -> bp.getMemoryUsed)
      out += ("jvm_buffer_" + name + "_max" -> bp.getTotalCapacity)
    }

    Json.toJson(out.toMap)
  }

  @no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET /hbc-microservice-template/admin/hystrix-demo

    DESCRIPTION
      Basic test of Hystrix

    RESULT
      Response
                                         """)
  def hystrixDemo = Action {
    Ok(DemoCommand().execute().toString)
  }
}
