

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.specs2.mock._

class RegionInfoSpec extends Specification with Mockito {

  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size (11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }

  isolated
  "The Mocked list" should {
    val m = mock[java.util.List[String]] // a concrete class would be mocked with: mock[new java.util.LinkedList[String]]

    "return a stubbed value" in {
      m.get(0) returns "one" // stub a method call with a return value
      m.get(0) must_== "one" // call the method
    }
    "verify that a method was called" in {
      m.get(0) returns "one" // stub a method call with a return value
      m.get(0) // call the method
      there was one(m).get(0) // verify that the call happened
    }
    "verify that a method was not called" in {
      there was no(m).get(0) // verify that the call never happened  }
    }
  }
}