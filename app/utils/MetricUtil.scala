package utils

import models.MetricDef
import java.util.Date

/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
object MetricUtil {

  def findLongestCompactionInLastWeek(regionName:String) : Option[(Long, Date)] = {
    findLongestCompactionInLastWeek(MetricDef.COMPACTIONS(regionName))
  }

  def findLongestCompactionInLastWeek(metricDef:MetricDef) : Option[(Long, Date)] = {
    var timestamp = MetricDef.now()-1000*3600*24*7;
    val metric = metricDef.metric(timestamp, MetricDef.now())
    var result:(Long, Date) = (0L, null)

    metric.values.foreach { record =>
      if(record.v > 0)
        timestamp = record.ts
      else {
        val duration = record.ts - timestamp
        if(duration > result._1) {
          result = (duration, new Date(timestamp))
        }
      }
    }

    if(result._1 > 0L) {
      Some(result)
    } else {
      None
    }
  }

  def findLongestCompactionInLastWeek() : Option[(Long, Date, String)] = {
    val metrics = MetricDef.findByName(MetricDef.COMPACTIONS)
    var result:(Long, Date, String) = (0L, null, "")

    metrics.foreach { metricDef =>
      findLongestCompactionInLastWeek(metricDef) match {
        case Some(longestCompaction) =>
          if(longestCompaction._1 > result._1) {
            result = (longestCompaction._1, longestCompaction._2, metricDef.targetDesc)
          }
        case None =>
      }
    }

    if(result._1 > 0L) {
      Some(result)
    } else {
      None
    }
  }
}
