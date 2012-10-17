package controllers

import play.api._
import play.api.mvc._
import java.net.URLDecoder
import collection.mutable.MutableList
import collection.Map
import models.{Metric, MetricDef}
import com.codahale.jerkson.Json._
import com.codahale.jerkson.Json
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Regions extends Controller {

  def index() = Action {
    Async {
      models.Region.allAsync().map { regionInfos => Ok(views.html.regions.index(regionInfos)) }
    }
  }

  def redirectToShow(regionName: String) = Action {
    Redirect(routes.Regions.show(regionName))
  }

  def show(regionName: String) = Action {
    val decodedRegionName = URLDecoder.decode(regionName, "UTF-8")
    Async {
      models.Region.findByNameAsync(decodedRegionName).map { region =>
        if (region == null)
          NotFound
        else {
      	  val info = region.getRegionInfo()
          Ok(views.html.regions.show(region, info, region.findLongestCompactionInLastWeek()))
        }
      }
    }
  }

  def listJson = Action { request =>
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