package controllers

import play.api._

import play.api.mvc._
import play.api.libs.json._

import helpers.ControllerPayloadLike._
import helpers.ControllerTimeoutLike._
import helpers.AdminHelper._

object Admin extends Controller {

  @no.samordnaopptak.apidoc.ApiDoc(doc = """
    GET /hbc-microservice-template/admin/ping

    DESCRIPTION
      Basic health check

    RESULT
      Response

  """)
  def ping = Action.async {
    implicit request =>
      timeout {
        Logger.debug("ping")
        writeResponseGet("pong")
      }
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
  def jvmstats = Action.async {
    implicit request =>
      timeout {
        Logger.debug("jvmstats")
        writeResponseGet(Json.toJson(extractJvmStats()))
      }
  }
}
