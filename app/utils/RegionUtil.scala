package utils

import models.{Region, MetricDef}
import java.util.Date

/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
object RegionUtil {
  case class TableSizeStatistics(var min:Double, var max:Double, var stdDev:Double)

  def regionStatisticsByTable:Map[String, TableSizeStatistics] = {
    Region.all.groupBy(_.tableName).mapValues { regions =>
      val sizes = regions.map(_.storefileSizeMB)
      TableSizeStatistics(
        sizes.min,
        sizes.max,
        stats.stdDev(sizes)
      )
    }
  }

  def regionSizes:Map[String, Double] = {
    Region.all.groupBy(_.serverName).mapValues { regions =>
      regions.map(_.storefileSizeMB).sum
    }
  }
}