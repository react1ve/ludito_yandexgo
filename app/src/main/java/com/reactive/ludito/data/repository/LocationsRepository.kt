package com.reactive.ludito.data.repository

import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.data.db.LocationDao
import com.reactive.ludito.data.db.LocationMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface LocationsRepository {
    val allSavedLocations: Flow<List<LocationInfo>>
    suspend fun saveLocation(locationInfo: LocationInfo)
    suspend fun deleteLocation(locationInfo: LocationInfo)
    suspend fun isLocationSaved(locationId: String): Boolean
}

class LocationsRepositoryImpl(private val locationDao: LocationDao) : LocationsRepository {

    override val allSavedLocations: Flow<List<LocationInfo>> = locationDao.getAllLocations()
        .map { entities -> entities.map { LocationMapper.mapEntityToLocationInfo(it) } }

    override suspend fun saveLocation(locationInfo: LocationInfo) {
        val entity = LocationMapper.mapLocationInfoToEntity(locationInfo)
        locationDao.insertLocation(entity)
    }

    override suspend fun deleteLocation(locationInfo: LocationInfo) {
        val entity = LocationMapper.mapLocationInfoToEntity(locationInfo)
        locationDao.deleteLocation(entity)
    }

    override suspend fun isLocationSaved(locationId: String): Boolean {
        return locationDao.isLocationSaved(locationId) > 0
    }
}