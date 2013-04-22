/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import utils.MetricUtil
import models.RegionName

object Api extends Controller {

  def heartbeat = Action { implicit request =>
    Ok(stringify(toJson(Map(
      "status" -> toJson("OK")
    )))).as("application/json")
  }

  def dashboard = Action { implicit request =>
    Ok(stringify(toJson(Map(
      "compaction_duration" -> compactionDuration
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
}
