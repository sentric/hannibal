/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */
package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import models.{Region, MetricDef, Table}
import play.api.libs.concurrent.{Promise, Akka}
import com.codahale.jerkson.Json._
import play.api.Play.current
import scala.collection.mutable.ListBuffer

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


  def regions = Action {
    implicit request =>
      Async {
        Akka.future {
          var result = ListBuffer[Region]()
          if (request.queryString.contains("table") ) {
            request.queryString("table").foreach(table =>
              result ++= models.Region.forTable(table).toList
            )
          } else {
             result ++= models.Region.all().toList
          }
          Ok(generate(result.toList)).as("application/json")
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

  def metricsByTarget(target: String) = Action {
    implicit request =>
      val until = MetricDef.now()
      val since = until - (if (request.queryString.contains("range")) request.queryString("range")(0).toLong else 1000 * 60 * 60 * 24)
      val metricNames = if (request.queryString.contains("metric")) request.queryString("metric") else MetricDef.ALL_REGION_METRICS

      Async {
        Akka.future {
          val metrics = metricNames.map {
            metricName =>
              MetricDef.findRegionMetricDef(target, metricName).metric(since, until)
          }

          Ok(generate(metrics)).as("application/json")
        }
      }
  }

}
