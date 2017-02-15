package org.sf1bacon.googleextractor


import com.google.maps.model.PlaceDetails

/**
  * Created by agapito on 17/02/2017.
  */

/** Stores the google places data for a venue
  *
  * @param name             name of the place
  * @param address          address map for the place (keys are `street_number`, `route`, `locality`, `postal_town`,
  *                         `administrative_area_level_2`, `administrative_area_level_1`, `country`, `postal_code` )
  *                         See `com.google.maps.model.AddressComponentType`.
  * @param formattedAddress string with a simple address for the place
  * @param phoneNumber      international phone number
  * @param latlong          geographic locations
  * @param vicinity         vicinity of the place
  * @param googleRating     overall google average rating
  * @param priceLevel       google price level
  * @param website          place's private website
  * @param googleURL        link to google's place info
  * @param googleID         google placeID for this place
  * @param reviews          option with a list of [[Review]]s for this place
  */
case class Place(name: String,
                 address: Map[String, String],
                 formattedAddress: String,
                 phoneNumber: String,
                 latlong: (Double, Double),
                 vicinity: String,
                 googleRating: Double,
                 priceLevel: String,
                 website: String,
                 googleURL: String,
                 googleID: String,
                 reviews: Option[List[Review]])

object Place {

  /** [[Place]] constructor from `com.google.maps.model.PlaceDetails`
    *
    * @param place
    * @return new [[Place]] instance
    */
  def apply(place: PlaceDetails): Place =

    new Place(
      place.name,
      place.addressComponents.map(c => (c.types(0).toString, c.longName)).toMap,
      place.formattedAddress,
      place.internationalPhoneNumber,
      (place.geometry.location.lat, place.geometry.location.lng),
      place.vicinity,
      place.rating,
      s"${place.priceLevel}",
      s"${place.website}",
      s"${place.url}",
      place.placeId,
      //scalastyle:off null
      if (place.reviews != null) Some(place.reviews.map(r => Review(r.rating, r.text, r.time)).toList) else None
      //scalastyle:on null
    )
}
