/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import java.net.URLDecoder
import models.Table
import utils.MetricUtil
import java.text.DateFormat

object Regions extends Controller {

  def index() = Action {
    implicit request =>
      Async {
        models.Region.allAsync().map {
          regionInfos => Ok(views.html.regions.index(regionInfos))
        }
      }
  }

  def redirectToShow(regionName: String) = Action {
    implicit request =>
      Redirect(routes.Regions.show(regionName))
  }

  def show(regionName: String) = Action {
    implicit request =>
      val decodedRegionName = URLDecoder.decode(regionName, "UTF-8")
      Async {
        models.Region.findByNameAsync(decodedRegionName).map {
          region =>
            if (region == null)
              NotFound
            else {
              val info = region.getRegionInfo()
              val table = Table.findByName(region.tableName)
              val longestCompactionString = MetricUtil.findLongestCompactionInLastWeek(region.regionName) match {
                case Some(longestCompaction) =>
                  "%.1fs at %s".format(longestCompaction._1 / 1000.0, DateFormat.getInstance().format(longestCompaction._2))
                case None =>
                  "None"
              }

              Ok(views.html.regions.show(region, info, table, longestCompactionString))
            }
        }
      }
  }
}
