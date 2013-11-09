/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package actors

import akka.actor.Actor
import play.api.Logger
import java.util.Date
import models.{LogFile, Compaction, MetricDef}
import actors.UpdateMetricsActor._
import play.libs.Akka
import akka.util.duration._

object UpdateMetricsActor {
  val UPDATE_REGION_INFO_METRICS = "updateRegionInfoMetrics"
  val INIT_COMPACTION_METRICS = "initCompactionMetrics"
  val UPDATE_COMPACTION_METRICS = "updateCompactionMetrics"
  val CLEAN_OLD_METRICS = "cleanOldMetrics"
}

class UpdateMetricsActor extends Actor {
  def receive = {

    case UPDATE_REGION_INFO_METRICS  =>
      updateMetrics("RegionMetrics", () => {
        var updated = 0
        val regions = models.Region.all()
        regions.foreach { regionInfo =>
          if(MetricDef.STOREFILE_SIZE_MB(regionInfo.regionName).update(regionInfo.storefileSizeMB))
            updated = updated + 1
          if(MetricDef.STOREFILES(regionInfo.regionName).update(regionInfo.storefiles))
            updated = updated + 1
          if(MetricDef.MEMSTORE_SIZE_MB(regionInfo.regionName).update(regionInfo.memstoreSizeMB))
            updated = updated + 1
        }
        updated
      })

    case INIT_COMPACTION_METRICS =>
      if(LogFile.init()) {
        Akka.system.scheduler.scheduleOnce(10 seconds, context.self, UpdateMetricsActor.UPDATE_COMPACTION_METRICS)
      } else {
        Logger.error("Compaction metrics update disabled because discovery of the log file url pattern failed. "+
          "Please check your compactions.logfile-path-pattern in application.conf.")
      }

    case UPDATE_COMPACTION_METRICS =>
      updateMetrics("CompactionMetrics", () => {
        try {
          var updated = 0
          val compactions = models.Compaction.all()
          val regions = models.Region.all()
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
           Akka.system.scheduler.scheduleOnce(30 seconds, context.self, UpdateMetricsActor.UPDATE_COMPACTION_METRICS)
        }

      })

    case CLEAN_OLD_METRICS =>
      Logger.info("start cleaning metrics and records older than one week... (" + new Date() + ")")
      val before = System.currentTimeMillis()
      val cleaned = MetricDef.clean()
      val after = System.currentTimeMillis()
      Logger.info("cleaned " + cleaned._1 + " old metrics and " + cleaned._2 + " old records , took " + (after - before) + "ms... (" +new Date()+") ")
  }

  def updateMetrics(name:String, functionBlock: () => Int) : Unit = {
    Logger.info("start updating " + name + "... (" + new Date() + ")")
    val before = System.currentTimeMillis()
    val length = functionBlock()
    val after = System.currentTimeMillis()
    Logger.info("completed updating " + length + " " + name + ", took " + (after - before) + "ms... (" +new Date()+") ")
  }
}