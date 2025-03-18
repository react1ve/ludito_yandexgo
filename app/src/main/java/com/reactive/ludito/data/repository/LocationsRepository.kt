package com.reactive.ludito.data.repository

import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.data.room.LocationsDatabase
import com.reactive.ludito.data.room.SavedLocationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationsRepository(private val database: LocationsDatabase) {

    private val savedLocationsDao = database.savedLocationsDao()

    val allSavedLocations: Flow<List<LocationInfo>> = savedLocationsDao.getAllSavedLocations()
        .map { entities -> entities.map { it.toLocationInfo() } }

    suspend fun saveLocation(locationInfo: LocationInfo) {
        savedLocationsDao.saveLocation(SavedLocationEntity.fromLocationInfo(locationInfo))
    }

    suspend fun deleteLocation(locationInfo: LocationInfo) {
        savedLocationsDao.deleteLocationById(locationInfo.id)
    }

    suspend fun getLocationById(id: String): LocationInfo? {
        return savedLocationsDao.getLocationById(id)?.toLocationInfo()
    }

    suspend fun isLocationSaved(id: String): Boolean {
        return savedLocationsDao.getLocationById(id) != null
    }

    suspend fun clearAllSavedLocations() {
        savedLocationsDao.deleteAllLocations()
    }
}