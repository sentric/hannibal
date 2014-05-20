/*
 * Copyright 2014 YMC. See LICENSE for details.
 */
package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models.{MetricDef, Table}
import com.codahale.jerkson.Json._
import java.util.concurrent.TimeUnit

object Api extends Controller {

  def heartbeat() = Action { implicit request =>
    val heartBeatOk = toJson(Map("status" -> "OK"))
    Ok(heartBeatOk).as(JSON)
  }

  def tables() = Action { implicit request =>
    Ok(generate(Table.all())).as(JSON)
  }

  def regions() = Action { implicit request =>
    val tables = request.queryString.get("table").flatten.toSet

    val regions = if(tables.isEmpty) {
      models.Region.all()
    } else {
      tables map { tableName  =>
        models.Region.forTable(tableName)
      } flatten
    }

    Ok(generate(regions)).as(JSON)
  }

  def metrics() = Action { implicit request =>
    val (since, until) = parsePeriod
    val metricNames = parseMetricNames

    val metrics = metricNames.map { metricName =>
      MetricDef.findByName(metricName).map { metricDef =>
        metricDef.metric(since, until)
      }
    }
    Ok(generate(metrics.flatten)).as(JSON)
  }

  def metricsByTarget(target: String) = Action { implicit request =>
    val (since, until) = parsePeriod
    val metricNames = parseMetricNames

    val metrics = metricNames.map { metricName =>
      MetricDef.findRegionMetricDef(target, metricName).metric(since, until)
    }

    Ok(generate(metrics)).as(JSON)
  }

  def parseMetricNames(implicit request: Request[_]) =
    if (request.queryString.contains("metric")) request.queryString("metric").toSet else MetricDef.ALL_REGION_METRICS

  def parsePeriod(implicit request: Request[_]) = {
    val until = System.currentTimeMillis()
    val since = until - request.queryString.get("range").flatMap(_.headOption).map(_.toLong).getOrElse(TimeUnit.DAYS.toMillis(1))
    (since, until)
  }
}
