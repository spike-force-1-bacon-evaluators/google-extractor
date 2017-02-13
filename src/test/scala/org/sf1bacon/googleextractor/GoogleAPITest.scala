package org.sf1bacon.googleextractor

import com.google.maps.NearbySearchRequest
import com.google.maps.model.{LatLng, PlaceType, PlacesSearchResponse, RankBy}
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
    assert(restaurantSearch((51.508557, -0.127612), 50000).length == 60)
    // scalastyle:on magic.number
  }

}
