package com.reactive.ludito.ui.screens.tabs.map.details

import androidx.lifecycle.viewModelScope
import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.data.repository.LocationsRepository
import com.reactive.premier.base.BasePremierViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(private val repository: LocationsRepository) : BasePremierViewModel() {

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    fun checkIfSaved(locationId: String) {
        viewModelScope.launch {
            _isSaved.value = repository.isLocationSaved(locationId)
        }
    }

    fun addToFavorite(locationInfo: LocationInfo?) {
        locationInfo?.let {
            viewModelScope.launch {
                _isSaved.value = !_isSaved.value
                if (_isSaved.value.not()) {
                    repository.deleteLocation(it)
                } else {
                    repository.saveLocation(it)
                }
            }
        }
    }
}