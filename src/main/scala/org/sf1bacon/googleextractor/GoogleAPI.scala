package org.sf1bacon.googleextractor

import com.google.maps.errors.{InvalidRequestException, OverDailyLimitException, OverQueryLimitException}
import com.google.maps.model._
import com.google.maps.{GeoApiContext, NearbySearchRequest, PlaceDetailsRequest}
import com.typesafe.config.ConfigFactory

import scala.annotation.tailrec

/**
  * Created by agapito on 09/02/2017.
  */
object GoogleAPI {

  val gContext: GeoApiContext = setContext("google.conf")

  def setContext(confFile: String): GeoApiContext = {
    val config = ConfigFactory.load(confFile)
    val myAPIKey = config.getString("google.key")

    new GeoApiContext().setApiKey(myAPIKey)
      .toggleifExceptionIsAllowedToRetry(classOf[InvalidRequestException], true)
      .toggleifExceptionIsAllowedToRetry(classOf[OverDailyLimitException], false)
      .toggleifExceptionIsAllowedToRetry(classOf[OverQueryLimitException], false)
  }

  def filterRestaurants(search: PlacesSearchResponse): List[PlacesSearchResult] = {
    search.results
      .filter(r => !r.types.contains("lodging") && !r.types.contains("meal_takeaway"))
      .toList
  }

  @tailrec
  def getAllResults(search: PlacesSearchResponse,
                    context: GeoApiContext,
                    result: List[PlacesSearchResult]
                   ): List[PlacesSearchResult] = {

    // scalastyle:off null
    // Using Java API which returns null
    if (search.nextPageToken == null) {
      // scalastyle:on null
      result
    }
    else {
      val nextPage = new NearbySearchRequest(context).pageToken(search.nextPageToken).await()
      getAllResults(nextPage, context, filterRestaurants(nextPage) ::: result)
    }

  }

  def restaurantSearch(center: (Double, Double), radius: Int): List[PlacesSearchResult] = {

    val search: PlacesSearchResponse =
      new NearbySearchRequest(gContext)
        .language("en")
        .keyword("restaurant")
        .location(new LatLng(center._1, center._2))
        .radius(radius)
        .`type`(PlaceType.RESTAURANT)
        .rankby(RankBy.PROMINENCE)
        .await()

    getAllResults(search, gContext, filterRestaurants(search))

  }

  def getPlaceInfo(googleID: String): PlaceDetails ={

    new PlaceDetailsRequest(gContext)
      .placeId(googleID)
      .await()

  }
}
