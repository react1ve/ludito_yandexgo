package com.reactive.ludito.data.db

import com.reactive.ludito.data.LocationInfo

object LocationMapper {
    fun mapEntityToLocationInfo(entity: LocationEntity): LocationInfo {
        return LocationInfo(
            id = entity.id,
            name = entity.name,
            address = entity.address,
            latitude = entity.latitude,
            longitude = entity.longitude,
            rating = entity.rating,
            feedbackCount = entity.feedbackCount
        )
    }

    fun mapLocationInfoToEntity(locationInfo: LocationInfo): LocationEntity {
        return LocationEntity(
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