/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models.{MetricDef, Table}
import com.codahale.jerkson.Json
import play.api.libs.concurrent.Akka
import com.codahale.jerkson.Json._
import play.api.Play.current

object Api extends Controller {

  def heartbeat = Action {
    implicit request =>
      Ok(stringify(toJson(Map(
        "status" -> toJson("OK")
      )))).as("application/json")
  }

  def tables = Action {
    implicit request =>
      Ok(com.codahale.jerkson.Json.generate(
        Table.all
      )).as("application/json")
  }


  def regions = Action { implicit request =>
    Async {
      models.Region.allAsync.map { regionInfos =>
        var filteredRegionInfos = regionInfos
        if (request.queryString.contains("table"))
          filteredRegionInfos = regionInfos.filter(i => request.queryString("table").contains(i.tableName))

        Ok(Json.generate(filteredRegionInfos)).as("application/json")
      }
    }
  }

  def metrics() = Action { implicit request =>
    val until = MetricDef.now()
    val since = until - (if (request.queryString.contains("range")) request.queryString("range")(0).toLong else 1000 * 60 * 60 * 24)
    val metricNames = if (request.queryString.contains("metric")) request.queryString("metric") else MetricDef.ALL_REGION_METRICS

    Async {
      Akka.future {
        val metrics =  metricNames.map { metricName =>
          MetricDef.findByName(metricName).map { metricDef =>
            metricDef.metric(since, until)
          }
        }
        Ok(generate(metrics.flatten)).as("application/json")
      }
    }
  }

  def metricsByTarget(target: String) = Action { implicit request =>
    val until = MetricDef.now()
    val since = until - (if (request.queryString.contains("range")) request.queryString("range")(0).toLong else 1000 * 60 * 60 * 24)
    val metricNames = if (request.queryString.contains("metric")) request.queryString("metric") else MetricDef.ALL_REGION_METRICS

    Async {
      Akka.future {
        val metrics = metricNames.map { metricName =>
          MetricDef.findRegionMetricDef(target, metricName).metric(since, until)
        }

        Ok(generate(metrics)).as("application/json")
      }
    }
  }

}
