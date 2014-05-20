package models

/*
* Copyright 2014 YMC. See LICENSE for details.
*/

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.cache.Cache
import scala.collection.Seq
import scala.collection.Map
import play.api.Play.current

class RegionSpec extends Specification  {

  "Region" should {

    "provide a method: #all" >> {

      // TODO reactivate when we are able to add a proper Mocking framework
//      "returning an empty list when cache not yet warmed" >> {
//        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//          val regions = Region.all
//          regions must equalTo(Seq())
//        }
//      }

      "returning the Cached Values when cache warmed" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = null; // TODO replace with a mock
          val originalRegions = Seq(region)
          Cache.set("regions.allRegions", originalRegions)
          val regions = Region.all
          regions must be(originalRegions)
        }
      }
    }

    "provide a method: #findByName" >> {
      // TODO reactivate when we are able to add a proper Mocking framework
//      "should return None when not found" >> {
//        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//          Region.findByName("somename") must beNone
//        }
//      }
//    }

    // TODO reactivate when we are able to add a proper Mocking framework
//    "provide a method: #forTable" >> {
//      "returning an empty list when cache not yet warmed" >> {
//        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//          val regions = Region.forTable("table")
//          regions must equalTo(Seq())
//        }
//      }

      "returning an empty list when table not found" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          Cache.set("regions.forTable", Map[String, Seq[Region]]())
          val regions = Region.forTable("table")
          regions must equalTo(Seq())
        }
      }

      "returning the list for the table when table found" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val region = null; // TODO replace with a mock
          val originalRegions = Seq(region)
          Cache.set("regions.forTable", Map[String, Seq[Region]]("table" -> originalRegions))
          val regions = Region.forTable("table")
          regions must be(originalRegions)
        }
      }
    }
  }
}
