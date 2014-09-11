/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
package controllers

import play.api.libs.json.Writes
import play.api.mvc._
import models.{Region, MetricDef, Table}
import play.libs.Json._
import play.api.libs.json._

import java.util.concurrent.TimeUnit
import play.api.Play

object Api extends Controller {



  def heartbeat() = Action { implicit request =>
    val heartBeatOk: JsValue =  JsObject(Seq("status" -> JsString("OK")))
    Ok(Json.stringify(heartBeatOk)).as(JSON)
  }

  def tables() = Action { implicit request =>
    Ok(stringify(toJson(Table.all()))).as(JSON) // TODO: toJson really required ???
  }

  def regions() = Action { implicit request =>
    val tables = request.queryString.get("table").getOrElse {
      Seq()
    }

    val regions = if(tables.isEmpty) {
      models.Region.all()
    } else {
      tables map { tableName  =>
        models.Region.forTable(tableName)
      } flatten
    }

    Ok(Json.stringify(Json.toJson(regions))).as(JSON)
  }

  def metrics() = Action { implicit request =>
    val (since, until) = parsePeriod
    val metricNames = parseMetricNames

    val metrics = metricNames.map { metricName =>
      MetricDef.findByName(metricName).map { metricDef =>
        metricDef.metric(since, until)
      }
    }
    Ok(stringify(toJson(metrics.flatten))).as(JSON)
  }

  def metricsByTarget(target: String) = Action { implicit request =>
    val (since, until) = parsePeriod
    val metricNames = parseMetricNames

    val metrics = metricNames.map { metricName =>
      MetricDef.findRegionMetricDef(target, metricName).metric(since, until)
    }

    Ok(stringify(toJson(metrics))).as(JSON)
  }

  def parseMetricNames(implicit request: Request[_]) =
    if (request.queryString.contains("metric")) request.queryString("metric").toSet else MetricDef.ALL_REGION_METRICS

  def parsePeriod(implicit request: Request[_]) = {
    val until = System.currentTimeMillis()
    val defaultRange:Int = Play.current.configuration.getInt("metrics.default-range").getOrElse(TimeUnit.DAYS.toSeconds(1).toInt)
    val since = until - request.queryString.get("range").flatMap(_.headOption).map(_.toLong).getOrElse(defaultRange * 1000L)
    (since, until)
  }
}
