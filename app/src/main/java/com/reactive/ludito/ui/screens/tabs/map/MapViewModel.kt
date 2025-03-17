package com.reactive.ludito.ui.screens.tabs.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.reactive.ludito.data.LocationInfo
import com.reactive.premier.base.BasePremierViewModel
import com.reactive.premier.utils.PremierConstants.Keys.geocoderKey
import com.reactive.premier.utils.extensions.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.core.component.inject
import java.net.URL
import java.util.Locale

class MapViewModel : BasePremierViewModel() {

    private val context by inject<Context>()

    suspend fun getAddressFromCoordinates(lat: String, lng: String): LocationInfo {
        val latitude = lat.toDoubleOrNull() ?: return LocationInfo("Invalid coordinates", "")
        val longitude = lng.toDoubleOrNull() ?: return LocationInfo("Invalid coordinates", "")

        return try {
            getAddressUsingYandexGeocoder(latitude, longitude)
        } catch (e: Exception) {
            loge(e, "Error getting address from coordinates")
            LocationInfo("Address not found", "")
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

                return LocationInfo(
                    name = name.capitalize(),
                    address = description.ifEmpty { name }.capitalize()
                )
            }
            return LocationInfo("Address not found", "")
        } catch (e: Exception) {
            loge(e, "Error parsing geocoder response")
            return LocationInfo("Address not found", "")
        }
    }

    private fun extractLocationInfo(address: Address): LocationInfo {
        val name = when {
            !address.featureName.isNullOrBlank() && address.featureName != address.thoroughfare -> address.featureName
            !address.premises.isNullOrBlank() -> address.premises
            !address.thoroughfare.isNullOrBlank() && !address.subThoroughfare.isNullOrBlank() ->
                "${address.thoroughfare} ${address.subThoroughfare}"

            !address.thoroughfare.isNullOrBlank() -> address.thoroughfare
            else -> "Location"
        }

        val addressComponents = mutableListOf<String>()

        if (!address.thoroughfare.isNullOrBlank()) {
            val street = if (!address.subThoroughfare.isNullOrBlank()) {
                "${address.thoroughfare}, ${address.subThoroughfare}"
            } else {
                address.thoroughfare
            }
            addressComponents.add(street)
        }

        if (!address.locality.isNullOrBlank()) {
            addressComponents.add(address.locality)
        }

        if (!address.adminArea.isNullOrBlank() && address.adminArea != address.locality) {
            addressComponents.add(address.adminArea)
        }

        if (!address.countryName.isNullOrBlank()) {
            addressComponents.add(address.countryName)
        }

        val fullAddress = addressComponents.joinToString(", ")

        return LocationInfo(
            name = name,
            address = fullAddress.ifEmpty { "Address not available" }
        )
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        return try {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            resultCode == ConnectionResult.SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getAddressUsingGoogleGeocoder(
        latitude: Double,
        longitude: Double
    ): LocationInfo {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var locationInfo = LocationInfo("Address not found", "")
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            locationInfo = extractLocationInfo(addresses[0])
                        }
                    }
                    return@withContext locationInfo
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        extractLocationInfo(addresses[0])
                    } else {
                        LocationInfo("Address not found", "")
                    }
                }
            } catch (e: Exception) {
                loge(e, "Google geocoder error")
                LocationInfo("Address not found", "")
            }
        }
    }

}