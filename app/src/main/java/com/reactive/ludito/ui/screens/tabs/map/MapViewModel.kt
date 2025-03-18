package com.reactive.ludito.ui.screens.tabs.map

import com.reactive.ludito.data.LocationInfo
import com.reactive.premier.base.BasePremierViewModel
import com.reactive.premier.utils.PremierConstants.Keys.geocoderKey
import com.reactive.premier.utils.extensions.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt


class MapViewModel : BasePremierViewModel() {

    suspend fun getAddressFromCoordinates(lat: String, lng: String): LocationInfo {
        val latitude = lat.toDoubleOrNull() ?: return LocationInfo("Invalid coordinates")
        val longitude = lng.toDoubleOrNull() ?: return LocationInfo("Invalid coordinates")

        return try {
            getAddressUsingYandexGeocoder(latitude, longitude)
        } catch (e: Exception) {
            loge(e, "Error getting address from coordinates")
            LocationInfo("Address not found")
        }
    }

    private suspend fun getAddressUsingYandexGeocoder(
        latitude: Double,
        longitude: Double
    ): LocationInfo {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://geocode-maps.yandex.ru/1.x/" +
                        "?apikey=$geocoderKey" +
                        "&geocode=$longitude,$latitude" +
                        "&format=json" +
                        "&lang=ru_RU"

                val response = URL(url).readText()
                parseYandexGeocoderResponse(response)
            } catch (e: Exception) {
                loge(e, "Yandex geocoder error")
                LocationInfo("Address not found", "")
            }
        }
    }

    private fun parseYandexGeocoderResponse(jsonResponse: String): LocationInfo {
        try {
            val json = JSONObject(jsonResponse)
            val featureMember = json
                .getJSONObject("response")
                .getJSONObject("GeoObjectCollection")
                .getJSONArray("featureMember")

            if (featureMember.length() > 0) {
                val geoObject = featureMember.getJSONObject(0).getJSONObject("GeoObject")

                val name = geoObject.getString("name")

                val description = if (geoObject.has("description")) {
                    geoObject.getString("description")
                } else {
                    try {
                        geoObject
                            .getJSONObject("metaDataProperty")
                            .getJSONObject("GeocoderMetaData")
                            .getString("text")
                    } catch (e: Exception) {
                        ""
                    }
                }
                val rating = try {
                    geoObject
                        .getJSONObject("metaDataProperty")
                        .getJSONObject("GeocoderMetaData")
                        .getJSONObject("Rating")
                        .getDouble("value")
                } catch (e: Exception) {
                    null // Rating not available
                }

                val feedbackCount = try {
                    geoObject
                        .getJSONObject("metaDataProperty")
                        .getJSONObject("GeocoderMetaData")
                        .getJSONObject("Rating")
                        .getInt("count")
                } catch (e: Exception) {
                    null // Feedback count not available
                }

                return LocationInfo(
                    name = name.capitalize(Locale.getDefault()),
                    address = description.ifEmpty { name }.capitalize(Locale.getDefault()),
                    rating = rating?.roundToInt() ?: 0,
                    feedbackCount = feedbackCount ?: 0

                )
            }
            return LocationInfo("Address not found")
        } catch (e: Exception) {
            loge(e, "Error parsing geocoder response")
            return LocationInfo("Address not found")
        }
    }
}