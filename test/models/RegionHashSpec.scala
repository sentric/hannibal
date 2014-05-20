package models

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class RegionHashSpec extends Specification  {

  "RegionHashSpec" should {

    "provide a method: #byName" >> {
      "should return correct Regionhash" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val regionHash = RegionHash.byName("a")
          regionHash.name must equalTo("a")
          regionHash.hash must equalTo(RegionHash.hash("a"))
        }
      }

      "should return not be equal to another hash" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val regionHashA = RegionHash.byName("a")
          val regionHashB = RegionHash.byName("b")
          regionHashA.name mustNotEqual(regionHashB)
        }
      }
    }
    "provide a method: #byHash" >> {
      "should return -unkown-" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val regionHash = RegionHash.byHash(RegionHash.hash("c"))
          regionHash.name must equalTo("-unknown-")
        }
      }

      "should return c" >> {
        running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
          val md5Sum = RegionHash.hash("c")
          RegionHash.byHash(md5Sum) // this should not be cached
          RegionHash.byName("c") // this should be cached
          val regionHash = RegionHash.byHash(md5Sum)
          regionHash.name must equalTo("c")
        }
      }
    }
  }
}