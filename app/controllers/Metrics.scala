/*
 * Copyright 2013 Sentric. See LICENSE for details.
 */

package controllers

import play.api.mvc._
import collection.mutable.MutableList
import models.{ Metric, MetricDef }
import com.codahale.jerkson.Json._
import com.codahale.jerkson.Json
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Metrics extends Controller {
  def listByTargetJson(target: String) = Action { implicit request =>
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

  def listJson() = Action { implicit request =>
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

  def showJson(target: String, metricName: String) = Action { implicit request =>
    val until = MetricDef.now()
    val since = until - (if (request.queryString.contains("range")) request.queryString("range")(0).toLong else 1000 * 60 * 60 * 24)
    Async {
      Akka.future {
        val metric = MetricDef.findRegionMetricDef(target, metricName).metric(since, until)
        Ok(generate(metric)).as("application/json")
      }
    }
  }
}