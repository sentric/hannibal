package controllers

/*
* Copyright 2013 Sentric. See LICENSE for details.
*/

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models.{RegionName, MetricDef}
import play.api.libs.json.Json._

class ApiSpec extends Specification  {

  args(skipAll = true) // TODO need a mocking framework

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

    "provide a method: /dashboard" >> {
      "returning 'none' when there is no longest compaction" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val result = routeAndCall(FakeRequest(GET, "/api/dashboard"));
          status(result.get) must equalTo(200)
          contentAsString(result.get) must contain("{\"compaction_duration\":\"none\"}")
        }
      }
      "returning the numbers for the compaction compaction" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d."
          val metricDef = MetricDef.findRegionMetricDef(region, "compactions")
          val anchor = MetricDef.now
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = routeAndCall(FakeRequest(GET, "/api/dashboard"));
          status(result.get) must equalTo(200)

          contentAsString(result.get) must contain("{\"compaction_duration\":{\"duration\":%d,\"timestamp\":%d,\"region\":\"%s\",\"table\":\"%s\"}}".format(
            1000,
            anchor - 2000,
            region,
            RegionName(region).tableName
          ))
        }
      }
      "returning 'none' when there is no region balance compaction" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val result = routeAndCall(FakeRequest(GET, "/api/dashboard"));
          status(result.get) must equalTo(200)
          contentAsString(result.get) must contain("{\"region_balance\":\"none\"}")
        }
      }
      "returning the numbers for the region balance" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d."
          val metricDef = MetricDef.findRegionMetricDef(region, "compactions")
          val anchor = MetricDef.now
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = routeAndCall(FakeRequest(GET, "/api/dashboard"));
          status(result.get) must equalTo(200)

          contentAsString(result.get) must contain("{\"compaction_duration\":{\"duration\":%d,\"timestamp\":%d,\"region\":\"%s\",\"table\":\"%s\"}}".format(
            1000,
            anchor - 2000,
            region,
            RegionName(region).tableName
          ))
        }
      }
    }
  }
}