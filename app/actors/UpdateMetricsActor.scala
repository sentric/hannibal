/*
 * Copyright 2014 YMC. See LICENSE for details.
 */

package actors

import akka.actor.Props
import play.api.Configuration

import java.util.Date
import akka.actor.Actor
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

import play.libs.Akka
import models.{LogFile, MetricDef}
import actors.UpdateMetricsActor._

object UpdateMetricsActor {

  val UPDATE_REGION_INFO = "updateRegionInfo"
  val INITIALIZE_LOGFILE = "initializeLogfile"
  val UPDATE_COMPACTION_METRICS = "updateCompactionMetrics"
  val CLEAN = "clean"

  private var cleanThresholdInSeconds: Int = 0
  private var cleanIntervalInSeconds: Int = 0
  private var regionsFetchIntervalInSeconds: Int = 0
  private var logfileFetchIntervalInSeconds: Int = 0

  def initialize(configuration : Configuration) = {

    LogFile.initialize( configuration )

    this.cleanThresholdInSeconds = configuration.getInt("metrics.clean-threshold").getOrElse(3600 * 24)
    this.cleanIntervalInSeconds = configuration.getInt("metrics.clean-interval").getOrElse(0)
    this.regionsFetchIntervalInSeconds = configuration.getInt("metrics.regions-fetch-interval").getOrElse(0)
    this.logfileFetchIntervalInSeconds = configuration.getInt("metrics.logfile-fetch-interval").getOrElse(0)

    val scheduler = Akka.system.scheduler
    val updateMetricsActor = Akka.system.actorOf(Props[UpdateMetricsActor], name = "updateMetricsActor")

    if(cleanIntervalInSeconds > 0) {
      scheduler.schedule(0 seconds, cleanIntervalInSeconds seconds, updateMetricsActor, UpdateMetricsActor.CLEAN)
    } else {
      Logger.warn("cleaning of old metrics is disabled in application.conf")
    }

    if(regionsFetchIntervalInSeconds < 1 ) {
      Logger.warn("ignoring invalid value %i for metrics.regions-fetch-interval, set it to ".format(regionsFetchIntervalInSeconds))
      regionsFetchIntervalInSeconds = 600
    }

    scheduler.schedule(2 seconds, regionsFetchIntervalInSeconds seconds, updateMetricsActor, UpdateMetricsActor.UPDATE_REGION_INFO)

    if(logfileFetchIntervalInSeconds > 0 ) {
      scheduler.scheduleOnce(3 seconds, updateMetricsActor, UpdateMetricsActor.INITIALIZE_LOGFILE)
    }  else {
      Logger.warn("fetching compaction metrics is disabled in application.conf")
    }
  }
}

class UpdateMetricsActor extends Actor {

  def receive = {

    case UPDATE_REGION_INFO  =>
      execute("refresh RegionInfo cache") {
        models.Region.refresh()
      }

      executeMetricUpdate("RegionMetrics") {
        var updated = 0
        val regions = models.Region.all
        regions.foreach { regionInfo =>
          if(MetricDef.STOREFILE_SIZE_MB(regionInfo.regionName).update(regionInfo.storefileSizeMB))
            updated = updated + 1
          if(MetricDef.STOREFILES(regionInfo.regionName).update(regionInfo.storefiles))
            updated = updated + 1
          if(MetricDef.MEMSTORE_SIZE_MB(regionInfo.regionName).update(regionInfo.memstoreSizeMB))
            updated = updated + 1
        }
        updated
      }

    case INITIALIZE_LOGFILE =>
      execute("initialize Logfile") {
        if(LogFile.init()) {
          Akka.system.scheduler.scheduleOnce(logfileFetchIntervalInSeconds seconds, context.self, UpdateMetricsActor.UPDATE_COMPACTION_METRICS)
        } else {
          Logger.error("Compaction metrics update disabled because discovery of the log file url pattern failed. "+
            "Please check your logfile.path-pattern in application.conf.")
        }
      }

    case UPDATE_COMPACTION_METRICS =>
      executeMetricUpdate("CompactionMetrics") {
        try {
          var updated = 0
          val compactions = models.Compaction.all
          val regions = models.Region.all
          regions.foreach { regionInfo =>
            val filteredCompactions = models.Compaction.forRegion(compactions, regionInfo.regionName)
            val metric = MetricDef.COMPACTIONS(regionInfo.regionName)
            filteredCompactions.foreach { compaction =>
              if (compaction.end.getTime > metric.lastUpdate) {
                if (!metric.update(1.0, compaction.start.getTime)) {
                  Logger.warn("possible bug: start compaction during compaction?")
                } else if(!metric.update(0.0, compaction.end.getTime)) {
                  Logger.warn("possible bug: end compaction outside compaction?")
                } else {
                  updated = updated + 1
                }
              }
            }
          }
          updated
        } finally {
           Akka.system.scheduler.scheduleOnce(logfileFetchIntervalInSeconds seconds, context.self, UpdateMetricsActor.UPDATE_COMPACTION_METRICS)
        }
      }

    case CLEAN =>
      Logger.info("start cleaning metrics and records older than one %d seconds... (%s)".format(cleanThresholdInSeconds, new Date()))
      val before = System.currentTimeMillis()
      val cleaned = MetricDef.clean(new java.util.Date().getTime() - 1000 * cleanThresholdInSeconds)
      val after = System.currentTimeMillis()
      Logger.info("cleaned " + cleaned._1 + " old metrics and " + cleaned._2 + " old records, took " + (after - before) + "ms... (" +new Date()+") ")
  }

  def executeMetricUpdate(name:String)(thunk: => Int)  {
    Logger.info("start updating " + name + "... (" + new Date() + ")")
    val before = System.currentTimeMillis()
    val length = thunk
    val after = System.currentTimeMillis()
    Logger.info("completed updating " + length + " " + name + ", took " + (after - before) + "ms... (" +new Date()+") ")
  }

  def execute(name:String)(thunk: => Unit) = {
    Logger.info("start " + name + "... (" + new Date() + ")")
    val before = System.currentTimeMillis()
    thunk
    val after = System.currentTimeMillis()
    Logger.info("completed " + name + ", took " + (after - before) + "ms... (" +new Date()+") ")
  }
}
