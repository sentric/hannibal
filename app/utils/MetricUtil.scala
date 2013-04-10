package utils

import models.MetricDef
import java.util.Date

/*
 * Copyright 2012 Sentric. See LICENSE for details.
 */
object MetricUtil {

  def findLongestCompactionInLastWeek(regionName:String) : (Long, Date) = {
    findLongestCompactionInLastWeek(MetricDef.COMPACTIONS(regionName))
  }

  def findLongestCompactionInLastWeek(metricDef:MetricDef) : (Long, Date) = {
    val metric = metricDef.metric(MetricDef.now()-1000*3600*24*7, MetricDef.now())
    var begin = metric.begin;
    var max = 0L;
    metric.values.foreach { record =>
      if(record.v > 0)
        begin = record.ts
      else
        max = scala.math.max(max, record.ts - begin)
    }
    (max, new Date(begin))
  }

  def findLongestCompactionInLastWeek() : (Long, Date, String) = {
    val metrics = MetricDef.findByName(MetricDef.COMPACTIONS)
    var result = (0L, new Date(), "")
    metrics.foreach { metricDef =>
      val longest = findLongestCompactionInLastWeek(metricDef)
      if(longest._1 > result._1) {
        result = (longest._1, longest._2, metricDef.targetDesc)
      }
    }
    result
  }
}
