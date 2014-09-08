package models

/*
* Copyright 2014 YMC. See LICENSE for details.
*/

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class MetricSpec extends Specification  {

  "MetricDef" should {

    "provide a method: #find" >> {
      "creating new metricDef when 'target-name'-combination does not yet exist" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          metricDef.target must equalTo("some-target")
          metricDef.name must equalTo("some-name")
        }
      }

      "returning the existing metricDef when 'target-name'-combination already exist" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          metricDef.update(99)

          MetricDef.find("some-target", "some-name", "desc").lastValue must equalTo(99)
        }
      }
    }

    "provide a method: #findByName" >> {
      "returning an empty list when no metricDef by the given 'name' exist" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDefs = MetricDef.findByName("some-name")
          metricDefs.size must equalTo(0)
        }
      }

      "returning a list of metricDefs when the given 'name' exists" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          MetricDef.find("some-target-1", "some-name", "desc")
          MetricDef.find("some-target-2", "some-name", "desc")
          MetricDef.find("some-target-3", "some-name", "desc")
          MetricDef.find("some-target-3", "some-name", "desc")

          val metricDefs = MetricDef.findByName("some-name")
          metricDefs.size must equalTo(3)
        }
      }
    }

    "provide a method: #update" >> {
      "setting not to new value when != previous value" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          metricDef.update(0) must equalTo(false)
        }
      }

      "setting to new value when == previous value" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          metricDef.update(1) must equalTo(true)
          metricDef.update(0) must equalTo(true)
        }
      }
    }

    "provide a method: #metric" >> {
      "returning a metric-object even when there are no recorded values" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          val now = MetricDef.now()
          val metric = metricDef.metric(0, now)
          metric.target must equalTo("some-target")
          metric.name must equalTo("some-name")
          metric.begin must equalTo(0)
          metric.end must equalTo(now)
          metric.values.size must equalTo(0)
          metric.prevValue must equalTo(0)
          metric.isEmpty must equalTo(true)
        }
      }

      "returning a metric-object with empty values when the given range is before recorded values" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          metricDef.update(1)
          metricDef.update(2)
          val metric = metricDef.metric(0, 1)
          metric.target must equalTo("some-target")
          metric.name must equalTo("some-name")
          metric.begin must equalTo(0)
          metric.end must equalTo(1)
          metric.values.size must equalTo(0)
          metric.prevValue must equalTo(2) //FIXME: actually this should be 0!
          metric.isEmpty must equalTo(false) //FIXME: actually this should be true!
        }
      }

      "returning a metric-object with empty values when the given range is after recorded values" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.find("some-target", "some-name", "desc")
          metricDef.update(1)
          metricDef.update(2)
          val now = MetricDef.now()
          val metric = metricDef.metric(now + 1, now + 2)
          metric.target must equalTo("some-target")
          metric.name must equalTo("some-name")
          metric.begin must equalTo(now + 1)
          metric.end must equalTo(now + 2)
          metric.values.size must equalTo(0)
          metric.prevValue must equalTo(2)
          metric.isEmpty must equalTo(false)
        }
      }


    }
  }
}