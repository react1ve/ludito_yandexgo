package com.reactive.ludito.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reactive.ludito.data.LocationInfo

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Float,
    val feedbackCount: Int
) {
    fun toLocationInfo(): LocationInfo {
        return LocationInfo(
            id = id,
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            rating = rating,
            feedbackCount = feedbackCount
        )
    }

    companion object {
        fun fromLocationInfo(locationInfo: LocationInfo): SavedLocationEntity {
            return SavedLocationEntity(
                id = locationInfo.id,
                name = locationInfo.name,
                address = locationInfo.address,
                latitude = locationInfo.latitude,
                longitude = locationInfo.longitude,
                rating = locationInfo.rating,
                feedbackCount = locationInfo.feedbackCount
            )
        }
    }
}