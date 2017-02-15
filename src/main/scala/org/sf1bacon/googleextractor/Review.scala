package org.sf1bacon.googleextractor

import org.joda.time.Instant

/**
  * Created by agapito on 17/02/2017.
  */

/** Google places review
  *
  * @param rating star rating (1 to 5)
  * @param text   text of the review
  * @param time   time in unix epoch (seconds since midnight January 1 1970 UTC)
  */
case class Review(rating: Int, text: String, time: Instant)

