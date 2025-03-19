package com.reactive.ludito.data.repository

import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.data.room.SavedLocationEntity
import com.reactive.ludito.data.room.SavedLocationsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationsRepository(private val savedLocationsDao: SavedLocationsDao) {

    val allSavedLocations: Flow<List<LocationInfo>> = savedLocationsDao.getAllSavedLocations()
        .map { entities -> entities.map { it.toLocationInfo() } }

    suspend fun saveLocation(locationInfo: LocationInfo) {
        savedLocationsDao.saveLocation(SavedLocationEntity.fromLocationInfo(locationInfo))
    }

    suspend fun deleteLocation(locationInfo: LocationInfo) {
        savedLocationsDao.deleteLocation(SavedLocationEntity.fromLocationInfo(locationInfo))
    }

    suspend fun isLocationSaved(id: String): Boolean {
        return savedLocationsDao.getLocationById(id) != null
    }
}