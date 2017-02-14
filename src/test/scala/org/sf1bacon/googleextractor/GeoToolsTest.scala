package org.sf1bacon.googleextractor

import scala.math._
import org.scalatest.FunSuite
import org.sf1bacon.googleextractor.GeoTools._

/**
  * Created by agapito on 14/02/2017.
  */
class GeoToolsTest extends FunSuite {

  test("Haversine distance is correct") {
    val KingCollege = (51.511461, -0.116025)
    val StPaul = (51.513812, -0.098344)
    val distance = 1251
    assert(abs(haversineDistance(KingCollege, StPaul) - distance) < 0.5)
  }

  test("Law of cosines is equivalent to haversine for small (~100m) distances") {
    val KingCollege = (51.511461, -0.116025)
    val TempleSt = (51.511116, -0.113980)
    // The accuracy is generraly much better (i.e. 3 or 4 decimal places) but even half a meter is sufficient here
    assert(abs(haversineDistance(KingCollege, TempleSt) - lawOfCosDistance(KingCollege, TempleSt)) < 0.5)
  }

  test("Law of cosines is equivalent to haversine for large (~10000m) distances") {
    val KingCollege = (51.511461, -0.116025)
    val LondonAirport = (51.504986, 0.049485)
    assert(abs(haversineDistance(KingCollege, LondonAirport) - lawOfCosDistance(KingCollege, LondonAirport)) < 0.5)
  }

  test("Longitude shift is correct") {

    val shift = 100.0
    val O2arena = (51.5, 0.0)
    val posShift = longitudeShift(O2arena, shift)
    val negShift = longitudeShift(O2arena, -shift)

    assert(posShift._2 > 0)
    assert(negShift._2 < 0)
    // calculated from the law of cosines, so checking with haversine
    assert(abs(100 - haversineDistance(O2arena, posShift)) < 1e-3)
    assert(abs(100 - haversineDistance(O2arena, negShift)) < 1e-3)

  }

  test("Latitude shift is correct") {

    val shift = 100.0
    val O2arena = (51.5, 0.0)
    val posShift = latitudeShift(O2arena, shift)
    val negShift = latitudeShift(O2arena, -shift)

    assert(posShift._1 > O2arena._1)
    assert(negShift._1 < O2arena._1)

    // calculated from haversine, so checking with the law of cosines
    assert(abs(100 - lawOfCosDistance(O2arena, posShift)) < 1e-3)
    assert(abs(100 - lawOfCosDistance(O2arena, negShift)) < 1e-3)
  }

  test("Longitude list generator is working correctly") {

    val O2arena = (51.5, 0.0)
    val result = longitudeGenList(O2arena, 1000.0, 100.0)
    val resultDistances = result.map(p => distance(O2arena, p))

    val referenceDistances = List(
      1000.0, 1000.0,
      900.0, 900.0,
      800.0, 800.0,
      700.0, 700.0,
      600.0, 600.0,
      500.0, 500.0,
      400.0, 400.0,
      300.0, 300.0,
      200.0, 200.0,
      100.0, 100.0, 0.0
    )


    // we should have a list with 21 elements
    assert(result.length == referenceDistances.length)

    // we should have 10 positive shifts
    assert(result.count(_._2 > O2arena._2) == 10)

    // and 10 negative shifts
    assert(result.count(_._2 < O2arena._2) == 10)

    // the latitude should not have changed
    assert(result.map(_._1).distinct.length == 1)

    // distance to origin matches the expected result
    result.indices.foreach { i =>
      assert(abs(resultDistances(i) - referenceDistances(i)) < 0.5)
    }

  }

  test("Latitude list generator is working correctly") {

    val O2arena = (51.5, 0.0)
    val result = latitudeGenList(O2arena, 1000.0, 100.0)
    val resultDistances = result.map(p => distance(O2arena, p))

    val referenceDistances = List(
      1000.0, 1000.0,
      900.0, 900.0,
      800.0, 800.0,
      700.0, 700.0,
      600.0, 600.0,
      500.0, 500.0,
      400.0, 400.0,
      300.0, 300.0,
      200.0, 200.0,
      100.0, 100.0, 0.0
    )


    // we should have a list with 21 elements
    assert(result.length == referenceDistances.length)

    // we should have 10 positive shifts
    assert(result.count(_._1 > O2arena._1) == 10)

    // and 10 negative shifts
    assert(result.count(_._1 < O2arena._1) == 10)

    // the longitude should not have changed
    assert(result.map(_._2).distinct.length == 1)

    // distance to origin matches the expected result
    result.indices.foreach { i =>
      assert(abs(resultDistances(i) - referenceDistances(i)) < 0.5)
    }

    // if we set discardOrigin to true only the origin should be left out
    val resultNoOrigin = latitudeGenList(O2arena, 1000.0, 100.0, discardOrigin = true)
    assert(result.diff(resultNoOrigin).head == O2arena)

  }


  test("Grid generator workin correctly") {
    val zero = (0.0, 0.0)

    val zero1 = longitudeShift(zero, 100.0)
    val zero_1 = longitudeShift(zero, -100.0)
    val zero2 = longitudeShift(zero, 200.0)
    val zero_2 = longitudeShift(zero, -200.0)

    val result = generateGrid(zero, 200.0, sideLengthToRadius(100.0))

    val correct = List(
      latitudeGenList(zero, 200.0, 100.0),
      latitudeGenList(zero1, 200.0, 100.0),
      latitudeGenList(zero_1, 200.0, 100.0),
      latitudeGenList(zero2, 200.0, 100.0),
      latitudeGenList(zero_2, 200.0, 100.0)
    ).flatten

    // a 5x5 grid is generated
    assert(result.length == 5 * 5)

    // the list contains the correct results
    assert(result.diff(correct) == Nil)

  }

}
