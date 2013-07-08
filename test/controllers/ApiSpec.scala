package controllers

/*
* Copyright 2013 Sentric. See LICENSE for details.
*/

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models.MetricDef
import play.api.mvc.AsyncResult
import play.api.test.FakeApplication

class ApiSpec extends Specification {

  "/api" should {
    "provide a method: /heartbeat" >> {
      "always returning OK" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val result = routeAndCall(FakeRequest(GET, "/api/heartbeat"));
          status(result.get) must equalTo(200)
          contentAsString(result.get) must equalTo("{\"status\":\"OK\"}")
        }
      }
    }

    "provide a method: /metrics" >> {
      "returning empty array when there are no recorded metrics" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val result = routeAndCall(FakeRequest(GET, "/api/metrics")).get.asInstanceOf[AsyncResult].result.value
          status(result.get) must equalTo(200)
          contentAsString(result.get) must equalTo("[]")
        }
      }
      "returning empty array when there are no recorded metrics for this metric" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d."
          val metricDef = MetricDef.findRegionMetricDef(region, "compactions")
          val anchor = MetricDef.now
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = routeAndCall(FakeRequest(GET, "/api/metrics?metric=regions")).get.asInstanceOf[AsyncResult].result.value
          status(result.get) must equalTo(200)
          contentAsString(result.get) must equalTo("[]")
        }
      }
      "returning empty array when there are no recorded metrics for this range" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d."
          val metricDef = MetricDef.findRegionMetricDef(region, "compactions")
          val anchor = MetricDef.now
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          metricDef.update(4, anchor - 500)
          val result = routeAndCall(FakeRequest(GET, "/api/metrics?metric=compactions&range=0")).get.asInstanceOf[AsyncResult].result.value
          status(result.get) must equalTo(200)
          contentAsString(result.get) must contain("\"values\":[],\"prevValue\":4.0,\"isEmpty\":false,\"targetDesc\":\"article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d.\"")
        }
      }
      "returning content array when there are recorded metrics for this metric" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d."
          val metricDef = MetricDef.findRegionMetricDef(region, "compactions")
          val anchor = MetricDef.now
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = routeAndCall(FakeRequest(GET, "/api/metrics?metric=compactions")).get.asInstanceOf[AsyncResult].result.value
          status(result.get) must equalTo(200)

          contentAsString(result.get) must contain("\"values\":[{\"ts\":%d,\"v\":%.1f},{\"ts\":%d,\"v\":%.1f}],\"prevValue\":%.1f,\"isEmpty\":false".format(
            anchor - 2000,
            1.0,
            anchor - 1000,
            0.0,
            0.0
          ))
        }
      }
    }

  }
}
