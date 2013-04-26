package utils

import models.{Region, MetricDef}
import java.util.Date

/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
object RegionUtil {

  def regionStatisticsByTable:Map[String, (Double, Double, Double)] = {
    var sizes = new scala.collection.mutable.HashMap[String, (Double, Double, Double)]();
    Region.all.foreach { region =>
      if(!sizes.contains(region.tableName)) {
        sizes += (region.tableName -> (Double.MaxValue, 0.0, 0.0))
      }
      val old = sizes(region.tableName)
      sizes(region.tableName) = (
        math.min(old._1, region.storefileSizeMB),
        math.max (old._2, region.storefileSizeMB), 0.0)
    }
    sizes.toMap
  }


  def regionSizes:Map[String, Double] = {
    var sizes = new scala.collection.mutable.HashMap[String, Double]();
    Region.all.foreach { region =>
      if(!sizes.contains(region.serverName)) {
        sizes += (region.serverName -> 0.0)
      }
      sizes(region.serverName) = sizes(region.serverName) + region.storefileSizeMB
      region.serverName
    }
    sizes.toMap
  }

}
