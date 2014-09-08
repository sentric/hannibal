package utils

/*
* Copyright 2014 YMC. See LICENSE for details.
*/

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import models.MetricDef
import java.util.Date

class MetricUtilSpec extends Specification  {
  "MetricUtil" should {
    "provide a method: #findLongestCompactionInLastWeek" >> {

      "returning None when there is no longest compaction" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val result = MetricUtil.findLongestCompactionInLastWeek()
          result must equalTo(None)
        }
      }

      "returning None when there are compactions but they are older than one week" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.findRegionMetricDef("article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d.", "compactions")
          val anchor = MetricDef.now - 1000 * 3600 * 24 * 8
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = MetricUtil.findLongestCompactionInLastWeek()
          result must equalTo(None)
        }
      }

      "returning Some[...] when there is only one compaction" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.findRegionMetricDef("article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d.", "compactions")
          val anchor = MetricDef.now - 1000 * 3600 * 24 * 6
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = MetricUtil.findLongestCompactionInLastWeek()
          result must equalTo(Some((1000L, new Date(anchor - 2000L), "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d.")))
        }
      }

      "returning the longest compaction when there are multiple compactions" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val metricDef = MetricDef.findRegionMetricDef("article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d.", "compactions")
          val anchor = MetricDef.now
          metricDef.update(1, anchor - 7000)
          metricDef.update(0, anchor - 6000)
          metricDef.update(1, anchor - 5000)
          metricDef.update(0, anchor - 3000)
          metricDef.update(1, anchor - 2000)
          metricDef.update(0, anchor - 1000)
          val result = MetricUtil.findLongestCompactionInLastWeek()
          result must equalTo(Some((2000L, new Date(anchor - 5000L), "article,com.redtra,1344630511217.7b6e9618bdac4d4251324e57b3e4084d.")))
        }
      }
    }
  }
}