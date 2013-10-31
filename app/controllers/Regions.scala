/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import java.text.DateFormat
import java.net.URLDecoder
import play.api.mvc._
import models.{Region, Table}
import utils.MetricUtil
import views.html.{regions => html}

object Regions extends Controller {

  def index() = Action { implicit request =>
    Ok(html.index(models.Region.all()))
  }

  def redirectToShow(regionName: String) = Action { implicit request =>
    Redirect(routes.Regions.show(regionName))
  }

  def show(regionName: String) = Action { implicit request =>
    val decodedRegionName = URLDecoder.decode(regionName, "UTF-8")

    Region.findByName(decodedRegionName).flatMap { region =>
      Table.findByName(region.tableName) map {table =>
        val longestCompactionString = MetricUtil.findLongestCompactionInLastWeek(region.regionName) match {
          case Some(longestCompaction) =>
            "%.1fs at %s".format(longestCompaction._1 / 1000.0, DateFormat.getInstance().format(longestCompaction._2))
          case None =>
            "None"
        }

        Ok(html.show(region, table, longestCompactionString))
      }
    } getOrElse NotFound
  }
}
