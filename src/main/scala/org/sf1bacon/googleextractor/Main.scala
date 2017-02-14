package org.sf1bacon.googleextractor

import org.sf1bacon.googleextractor.GoogleAPI._
import org.sf1bacon.googleextractor.GeoTools._

/**
  * Created by agapito on 09/02/2017.
  */
//noinspection ScalaStyle
object Main extends App {

  // scalastyle:off magic.number
  val results = restaurantSearch( (51.508557, -0.127612), 200)
  // scalastyle:on magic.number

  println(s"[INFO] Got data for ${results.length} places.")
  results.foreach { r => println(s"  ${r.name} (${r.types.mkString(",")})") }

}
