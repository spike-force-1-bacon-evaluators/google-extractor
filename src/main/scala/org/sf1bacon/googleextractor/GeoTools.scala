package org.sf1bacon.googleextractor

import scala.annotation.tailrec
import scala.math._

/**
  * Created by agapito on 13/02/2017.
  */
object GeoTools {

  // volumetric mean radius of earth in meters (cf. http://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html )
  val earthR = 6371008

  /**
    * Calculate the distance (in meters) between two points using the haversine formula
    *
    * @param p1 Coordinates for point 1
    * @param p2 Coordinates for point 2
    * @return The haversine distance (in meters) between the two points.
    */
  def haversineDistance(p1: (Double, Double), p2: (Double, Double)): Double = {

    val lat1 = toRadians(p1._1)
    val lon1 = toRadians(p1._2)

    val lat2 = toRadians(p2._1)
    val lon2 = toRadians(p2._2)

    val dlat = lat2 - lat1
    val dlon = lon2 - lon1

    // sin^2(∆lat/2) + cos(lat1) x cos(lat2) x sin^2(lon/2)
    val a = (sin(dlat / 2.0) * sin(dlat / 2.0)) + (cos(lat1) * cos(lat2) * sin(dlon / 2.0) * sin(dlon / 2.0))
    val c = 2 * asin(sqrt(a))

    c * earthR
  }

  /**
    * Calculate the distance (in meters) between two points using the law of cosines
    *
    * @param p1 Coordinates for point 1
    * @param p2 Coordinates for point 2
    * @return The law-of-cosines distance (in meters) between the two points.
    */
  def lawOfCosDistance(p1: (Double, Double), p2: (Double, Double)): Double = {

    val lat1 = toRadians(p1._1)
    val lon1 = toRadians(p1._2)

    val lat2 = toRadians(p2._1)
    val lon2 = toRadians(p2._2)

    val dlon = lon2 - lon1

    // distance = arccos( sin(lat1)sin(lat2) + cos(lat1)cos(lat2)cos(∆lon) ) x r
    acos((sin(lat1) * sin(lat2)) + (cos(lat1) * cos(lat2) * cos(dlon))) * earthR
  }

  // use the law of cosines by defaults because it is faster
  def distance(p1: (Double, Double), p2: (Double, Double)): Double = lawOfCosDistance(p1, p2)

  /**
    * Obtain the coordinates of a point longitude shifted from the origin by a given amount
    *
    * @param from origin of the shift (in degrees)
    * @param by   distance by which we want to shift the longitude (in meters)
    * @return the new point, with longitude shifted by the specified amount (in degrees)
    */
  def longitudeShift(from: (Double, Double), by: Double): (Double, Double) = {

    val lat = toRadians(from._1)
    val lon = toRadians(from._2)

    // Using the law of cosines because it is easier to solve for the longitude:
    // d = acos[sin^2(lat) + cos^2(lat)* cos(∆lon)] * r
    // cos(d/r) = sin^2(lat) + cos^(lat)* cos(∆lon)
    // [ cos(d/r) - sin^2(lat)] / cos^2(lat) = cos(∆lon)
    // ∆lon = acos{[ cos(d/r) - sin^2(lat)] / cos^2(lat) }
    //
    // newlon = acos{[ cos(d/r) - sin^2(lat)] / cos^2(lat) } + lon
    // or
    // newlon = -acos{[ cos(d/r) - sin^2(lat)] / cos^2(lat) } + lon

    val a = (cos(by / earthR) - (sin(lat) * sin(lat))) / (cos(lat) * cos(lat))

    val sgn = if (by >= 0) 1 else -1
    (from._1, toDegrees((sgn * acos(a)) + lon))
  }

  /**
    * Obtain the coordinates of a point latitude shifted from the origin by a given amount
    *
    * @param from origin of the shift (in degrees)
    * @param by   distance by which to shift the latitude (in meters)
    * @return the new point, with latitude shifted by the specified amount (in degrees)
    */
  def latitudeShift(from: (Double, Double), by: Double): (Double, Double) = {

    val lat = toRadians(from._1)
    val lon = toRadians(from._2)

    // Since ∆lon is zero haversine is much simpler:
    // d = 2*asin{ sqrt[sin^2(∆lat/2) + cos(lat1) x cos(lat2) x sin^2(lon/2)] } * r
    // d = 2*asin{ sqrt[sin^2(∆lat/2) + 0] }
    // d/(2r) = asin{ sqrt[sin^2(∆lat/2)] }
    // sin[d/(2r)] = sqrt[sin^2(∆lat/2)]
    // sin^2[d/(2r)] = sin^2(∆lat/2)
    // d/(2r) = ∆lat/2
    // d/r = ∆lat
    //
    // newlat = d/r  + lat

    (toDegrees(lat + (by / earthR)), from._2)

  }

  /**
    * Get the side length of a square inscribed in a circle with a known radius
    *
    * @param radius circle radius
    * @return inscribed squire side length
    */
  def radiusToSideLength(radius: Double): Double = sqrt(2) * radius

  /**
    * Get the radius a circle on which this square is inscribed
    *
    * @param side the square side lenght
    * @return the circle radius
    */
  def sideLengthToRadius(side: Double): Double = side / sqrt(2)

  /**
    * Generates a list of evenly spaced longitude shifted points from the origin
    *
    * @param origin      center point
    * @param maxDistance distance cutoff
    * @param squareSide  grid spacing
    * @param result      the list of points so far (default value is Nil)
    * @param nShift      number of shifts so far (loop counter, default value is 1.0)
    * @return
    */
  @tailrec
  def longitudeGenList(origin: (Double, Double),
                       maxDistance: Double,
                       squareSide: Double,
                       result: List[(Double, Double)] = Nil,
                       nShift: Double = 0.0
                      ): List[(Double, Double)] = {

    if (result == Nil) {

      longitudeGenList(
        origin,
        maxDistance,
        squareSide,
        origin :: result,
        nShift + 1.0
      )

    } else if (distance(origin, result.head) + (squareSide / 2.0) > maxDistance) {
      result
    } else {
      val posShift = longitudeShift(origin, nShift * squareSide)
      val negShift = longitudeShift(origin, -nShift * squareSide)

      longitudeGenList(
        origin,
        maxDistance,
        squareSide,
        posShift :: negShift :: result,
        nShift + 1.0
      )

    }

  }

  /**
    * Generates a list of evenly spaced latitude shifted points from the origin
    *
    * @param origin        center point
    * @param maxDistance   distance cutoff
    * @param squareSide    grid spacing
    * @param result        the list of points so far (default value is Nil)
    * @param nShift        number of shifts so far (loop counter, default value is 1.0)
    * @param discardOrigin flag to discard the origin from the list (default = false)
    * @return
    */
  @tailrec
  def latitudeGenList(origin: (Double, Double),
                      maxDistance: Double,
                      squareSide: Double,
                      result: List[(Double, Double)] = Nil,
                      nShift: Double = 0.0,
                      discardOrigin: Boolean = false
                     ): List[(Double, Double)] = {

    if (result == Nil) {

      latitudeGenList(
        origin,
        maxDistance,
        squareSide,
        origin :: result,
        nShift + 1.0,
        discardOrigin
      )

    } else if (distance(origin, result.head) + (squareSide / 2.0) > maxDistance) {
      if (discardOrigin) {
        result.filter(_ != origin)
      } else {
        result
      }
    } else {
      val posShift = latitudeShift(origin, nShift * squareSide)
      val negShift = latitudeShift(origin, -nShift * squareSide)

      latitudeGenList(
        origin,
        maxDistance,
        squareSide,
        posShift :: negShift :: result,
        nShift + 1.0,
        discardOrigin
      )

    }

  }

  /**
    * Generate a grid of points separated by a distance equal to the side lenght of a square
    * inscribed in a circle of a given radius
    *
    * @param origin       origin of the grid (in degrees)
    * @param maxDistance  maximum distance cutoff (in meters)
    * @param searchRadius size of the local search radius (in meters)
    * @return list of points mathing the grid specs (in degrees)
    */
  def generateGrid(origin: (Double, Double),
                   maxDistance: Double,
                   searchRadius: Double
                  ): List[(Double, Double)] = {

    val squareSide = radiusToSideLength(searchRadius)
    val longitudeList = longitudeGenList(origin, maxDistance, squareSide)
    longitudeList.flatMap(p => latitudeGenList(p, maxDistance, squareSide))
      .sortWith(distance(origin, _) < distance(origin, _))
  }

}
