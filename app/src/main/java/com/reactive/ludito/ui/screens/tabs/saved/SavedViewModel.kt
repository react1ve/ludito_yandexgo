package com.reactive.ludito.ui.screens.tabs.saved

import androidx.lifecycle.viewModelScope
import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.data.repository.LocationsRepository
import com.reactive.premier.base.BasePremierViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(private val repository: LocationsRepository) : BasePremierViewModel() {

    val savedLocations: StateFlow<List<LocationInfo>> = repository.allSavedLocations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun deleteLocation(locationInfo: LocationInfo) {
        viewModelScope.launch {
            repository.deleteLocation(locationInfo)
        }
    }
}