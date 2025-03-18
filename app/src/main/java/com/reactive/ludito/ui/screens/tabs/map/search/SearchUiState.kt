package com.reactive.ludito.ui.screens.tabs.map.search

import com.reactive.ludito.data.SearchAddress
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point

data class MapUiState(
    val query: String = "",
    val searchState: SearchState = SearchState.Off,
    val userLocation: Point? = null
)

sealed interface SearchState {
    data object Off : SearchState
    data object Loading : SearchState
    data object Error : SearchState
    data class Success(
        val items: List<SearchAddress>,
        val zoomToItems: Boolean,
        val itemsBoundingBox: BoundingBox,
    ) : SearchState
}