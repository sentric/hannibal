/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api._
import play.api.mvc._
import java.net.URLDecoder
import collection.mutable.MutableList
import collection.Map
import com.codahale.jerkson.Json._
import com.codahale.jerkson.Json
import play.api.libs.concurrent.Akka
import play.api.Play.current
import models.{Table, Metric, MetricDef}
import utils.MetricUtil

object Regions extends Controller {

  def index() = Action { implicit request =>
    Async {
      models.Region.allAsync().map { regionInfos => Ok(views.html.regions.index(regionInfos)) }
    }
  }

  def redirectToShow(regionName: String) = Action { implicit request =>
    Redirect(routes.Regions.show(regionName))
  }

  def show(regionName: String) = Action { implicit request =>
    val decodedRegionName = URLDecoder.decode(regionName, "UTF-8")
    Async {
      models.Region.findByNameAsync(decodedRegionName).map { region =>
        if (region == null)
          NotFound
        else {
      	  val info = region.getRegionInfo()
          val table = Table.findByName(region.tableName)
          Ok(views.html.regions.show(region, info, table, MetricUtil.findLongestCompactionInLastWeek(region.regionName)))
        }
      }
    }
  }

  def listJson = Action { implicit request =>
    Async {
      models.Region.allAsync.map { regionInfos =>
        var filteredRegionInfos = regionInfos
        if (request.queryString.contains("table"))
          filteredRegionInfos = regionInfos.filter(i => request.queryString("table").contains(i.tableName))

        Ok(Json.generate(filteredRegionInfos)).as("application/json")
      }
    }
  }
}