/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import java.net.URLDecoder
import models.Table
import utils.MetricUtil
import java.text.DateFormat
import play.api.libs.concurrent.Promise
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.Logger

object Regions extends Controller {

  def index() = Action {
    implicit request =>
      Async {
        Akka.future {
          Ok(views.html.regions.index(models.Region.all()))
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
        Akka.future {
          val region = models.Region.findByName(decodedRegionName)
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
