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

  /**
    * GeoApiContext used in all connections to all the GoogleAPI
    */
  val gContext: GeoApiContext = setContext("google.conf")

  /**
    * Set up a Google GeoApiContext instance used for all connections.
    * This requires a configuration placed in `src/main/resources`
    * with the API key:
    * {{{
    *   google {
    *     key = "YOUR API KEY"
    *   }
    * }}}
    *
    * @param confFile name of the configuration file
    * @return new `GeoApiContext` instance
    */

  def setContext(confFile: String): GeoApiContext = {
    val config = ConfigFactory.load(confFile)
    val myAPIKey = config.getString("google.key")

    new GeoApiContext().setApiKey(myAPIKey)
      .toggleifExceptionIsAllowedToRetry(classOf[InvalidRequestException], true)
      .toggleifExceptionIsAllowedToRetry(classOf[OverDailyLimitException], false)
      .toggleifExceptionIsAllowedToRetry(classOf[OverQueryLimitException], false)
  }

  /**
    * Filter unwanted results from the `PlacesSearchResponse` and convert them to `List`
    *
    * @param search `PlacesSearchResponse` instance of restaurant search results
    * @return List of `PlacesSearchResult`
    */
  def filterRestaurants(search: PlacesSearchResponse): List[PlacesSearchResult] = {
    search.results
      .filter(r => !r.types.contains("lodging") && !r.types.contains("meal_takeaway"))
      .toList
  }

  /**
    * Get all the results for the search by requesting the next pages (if available)
    *
    * @param search  `PlacesSearchResponse` resulting from a `NearbySearchRequest`
    * @param context `GeoApiContext` for the connection
    * @param result  List of `PlacesSearchResult` for the search
    * @return
    */
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

  /**
    * Search for restaurants
    *
    * @param center geographical center for the search
    * @param radius radius (in meters) for the search
    * @return List of `PlacesSearchResult` for the search
    */
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

  /**
    * Request info about a place
    *
    * @param googleID google placeID of the place
    * @return `PlaceDetails` instance
    */
  def getPlaceInfo(googleID: String): PlaceDetails = {

    new PlaceDetailsRequest(gContext)
      .placeId(googleID)
      .await()

  }
}
