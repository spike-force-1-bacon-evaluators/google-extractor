package org.sf1bacon.googleextractor

import com.google.maps.model.{ AddressComponentType, PlaceDetails}
import org.sf1bacon.googleextractor.GoogleAPI._
import org.sf1bacon.googleextractor.GeoTools._

/**
  * Created by agapito on 09/02/2017.
  */
//noinspection ScalaStyle
object Main extends App {

  // set origin to Trafalgar Square
  val origin = (51.508052, -0.128037)

  // set the search radius
  val searchRadius = 150

  // set the distance cutoff
  //val maxDistance = 16000
  val maxDistance = 600

  val points = generateGrid(origin, maxDistance, searchRadius)
  println(s"[INFO] Using a maximum distance ${maxDistance}m with a search radius of ${searchRadius}m requires ${points.length} points:")
  points.foreach(p => println(f"  (${p._1}%.6f,${p._2}%.6f) "))

  for (p <- points) {
    println(f"[INFO] Searching around point: (${p._1}%.6f,${p._2}%.6f)")
    val placeIDs = restaurantSearch(p, searchRadius).map(r => r.placeId)

    println(s"[INFO] Got data for ${placeIDs.length} places.")
    val places = placeIDs.map(getPlaceInfo).map(Place(_))
  }


}
