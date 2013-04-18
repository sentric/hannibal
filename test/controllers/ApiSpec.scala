package controllers

/*
* Copyright 2013 Sentric. See LICENSE for details.
*/

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class ApiSpec extends Specification  {
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
  }
}