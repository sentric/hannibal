/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import utils.{RegionUtil, MetricUtil}
import models.{Region, MetricDef, RegionName}

object Api extends Controller {

  def heartbeat = Action { implicit request =>
    Ok(stringify(toJson(Map(
      "status" -> toJson("OK")
    )))).as("application/json")
  }

  def dashboard = Action { implicit request =>
    Ok(stringify(toJson(Map(
      "compaction_duration" -> compactionDuration,
      "region_balance" -> regionBalance,
      "region_size" -> regionSize
    )))).as("application/json")
  }

  private def compactionDuration = {
    MetricUtil.findLongestCompactionInLastWeek() match {
      case Some(longestCompaction) =>
        val regionName = RegionName(longestCompaction._3)
        toJson(Map(
          "duration" -> toJson(longestCompaction._1),
          "timestamp" -> toJson(longestCompaction._2.getTime),
          "region" -> toJson(longestCompaction._3),
          "table" -> toJson(regionName.tableName)
        ))
      case None =>
        toJson("none")
    }
  }

  private def regionBalance = {
    val regionSizes: Map[String, Double] = RegionUtil.regionSizes

    var max = "none" -> 0.0;
    var min = "none" -> Double.MaxValue;
    val stdDev = utils.stats.stdDevBy(regionSizes)(_._2)

    regionSizes.foreach { entry =>
      if(entry._2 > max._2) {
        max = entry
      }
      if(entry._2 < min._2) {
        min = entry
      }
    }

    if (max._2 > 0 && min._2 < Int.MaxValue) {
      toJson(Map(
        "min_host" -> toJson(min._1),
        "min_size" -> toJson(min._2),
        "max_host" -> toJson(max._1),
        "max_size" -> toJson(max._2),
        "std_dev" -> toJson(stdDev)
      ))
    } else {
      toJson("none")
    }
  }

  private def regionSize = {
    var max = "none" -> 0.0;
    var min = "none" -> Double.MaxValue;
    var stdDev = "none" -> 0.0;

    RegionUtil.regionStatisticsByTable.foreach { entry =>
      val (table, stats) = entry

      if(stats.max > max._2) {
        max = (table, stats.max)
      }
      if(stats.min < min._2) {
        min = (table, stats.min)
      }
      if(stats.stdDev > stdDev._2) {
        stdDev = (table, stats.stdDev)
      }
    }
    if (max._2 > 0 && min._2 < Int.MaxValue) {
      toJson(Map(
        "min_table" -> toJson(min._1),
        "min_size" -> toJson(min._2),
        "max_table" -> toJson(max._1),
        "max_size" -> toJson(max._2),
        "std_dev_table" -> toJson(stdDev._1),
        "std_dev_value" -> toJson(stdDev._2)
      ))
    } else {
      toJson("none")
    }
  }
}
