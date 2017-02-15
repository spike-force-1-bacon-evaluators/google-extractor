package org.sf1bacon.googleextractor

import org.sf1bacon.googleextractor.GoogleAPI._
import org.scalatest.FunSuite

import scala.util.{Failure, Success, Try}

/**
  * Created by agapito on 10/02/2017.
  */
class GoogleAPITest extends FunSuite {

  test("Context can't be set when the config file doesn't exist") {
    val test = Try(setContext("NoSuchFile.conf"))
    assert(
      test match {
        case Success(_) => false
        case Failure(e) =>
          //e.printStackTrace()
          true
      }
    )
  }

  test("Context created correctly") {
    val test = Try(setContext("google.conf"))
    assert(
      test match {
        case Success(_) => true
        case Failure(e) =>
          //e.printStackTrace()
          false
      }
    )
  }

  test("Can get data from Google Places") {
    // scalastyle:off magic.number
    val search = restaurantSearch((51.508557, -0.127612), 50000)
    // scalastyle:on magic.number

    val testID = search.head.placeId
    val testPlace = getPlaceInfo(testID)

    // searching with the maximum radius (50km) should return 60 results (the maximum that can be returned)
    assert(search.length == 60)

    // searching for a placeID should return info
    assert(testPlace.name != "")
  }

}
