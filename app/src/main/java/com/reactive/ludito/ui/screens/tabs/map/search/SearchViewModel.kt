package com.reactive.ludito.ui.screens.tabs.map.search

import androidx.lifecycle.viewModelScope
import com.reactive.ludito.data.SearchAddress
import com.reactive.premier.base.BasePremierViewModel
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.map.VisibleRegion
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.Session.SearchListener
import com.yandex.runtime.Error
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@Suppress("UNNECESSARY_SAFE_CALL")
class SearchViewModel : BasePremierViewModel() {

    private var searchItems: List<SearchAddress> = emptyList()
    private var searchSession: Session? = null
    private var zoomToSearchResult = false
    private val region = MutableStateFlow<VisibleRegion?>(null)
    private val searchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }

    @OptIn(FlowPreview::class)
    private val throttledRegion = region.debounce(1.seconds)
    private val query = MutableStateFlow("")
    private val searchState = MutableStateFlow<SearchState>(SearchState.Off)

    val uiState: StateFlow<MapUiState> = combine(
        query,
        searchState,
    ) { query, searchState ->
        MapUiState(
            query = query,
            searchState = searchState,
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, MapUiState())

    fun setQueryText(value: String) {
        query.value = value
    }

    fun setVisibleRegion(region: VisibleRegion) {
        this.region.value = region
    }

    fun startSearch(searchText: String? = null) {
        val text = searchText ?: query.value
        if (query.value.isEmpty()) return
        val region = region.value?.let {
            VisibleRegionUtils.toPolygon(it)
        } ?: return

        submitSearch(text, region)
    }

    fun reset() {
        searchSession?.cancel()
        searchSession = null
        searchState.value = SearchState.Off
        query.value = ""
    }

    fun subscribeForSearch(): Flow<*> {
        return throttledRegion.filter { it != null }
            .filter { searchState.value is SearchState.Success }
            .mapNotNull { it }
            .onEach { region ->
                searchSession?.let {
                    it.setSearchArea(VisibleRegionUtils.toPolygon(region))
                    it.resubmit(searchSessionListener)
                    searchState.value = SearchState.Loading
                    zoomToSearchResult = false
                }
            }
    }

    private val searchSessionListener = object : SearchListener {
        override fun onSearchResponse(response: Response) {
            searchItems = response.collection.children.mapNotNull { searchResult ->
                val obj = searchResult.obj ?: return@mapNotNull null
                val point = obj.geometry.firstOrNull()?.point ?: return@mapNotNull null

                val name = obj.name ?: ""

                val address = obj.descriptionText ?: ""

                var distance = ""
                var rating = 0
                var feedbacks = 0

                try {
                    obj.metadataContainer.let { container ->
                        try {
                            val distanceMetadata =
                                container.getItem(com.yandex.mapkit.search.ToponymResultMetadata::class.java)
                            if (distanceMetadata != null) {
                                distanceMetadata::class.java.getDeclaredField("formattedDistance")
                                    ?.let { field ->
                                        field.isAccessible = true
                                        val distanceValue = field.get(distanceMetadata)
                                        if (distanceValue != null) {
                                            distance = distanceValue.toString()
                                        }
                                    }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            val businessMetadata =
                                container.getItem(com.yandex.mapkit.search.BusinessObjectMetadata::class.java)
                            if (businessMetadata != null) {
                                try {
                                    businessMetadata::class.java.getDeclaredField("rating")
                                        ?.let { field ->
                                            field.isAccessible = true
                                            val ratingValue = field.get(businessMetadata)
                                            if (ratingValue != null && ratingValue is Number) {
                                                rating = ratingValue.toInt()
                                            }
                                        }
                                } catch (e: Exception) {
                                    try {
                                        businessMetadata::class.java.getDeclaredField("score")
                                            ?.let { field ->
                                                field.isAccessible = true
                                                val scoreValue = field.get(businessMetadata)
                                                if (scoreValue != null && scoreValue is Number) {
                                                    rating = scoreValue.toInt()
                                                }
                                            }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                try {
                                    businessMetadata::class.java.getDeclaredField("reviewCount")
                                        ?.let { field ->
                                            field.isAccessible = true
                                            val countValue = field.get(businessMetadata)
                                            if (countValue != null && countValue is Number) {
                                                feedbacks = countValue.toInt()
                                            }
                                        }
                                } catch (e: Exception) {
                                    try {
                                        businessMetadata::class.java.getDeclaredField("reviews")
                                            ?.let { field ->
                                                field.isAccessible = true
                                                val reviewsValue = field.get(businessMetadata)
                                                if (reviewsValue != null && reviewsValue is Number) {
                                                    feedbacks = reviewsValue.toInt()
                                                }
                                            }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val id = (name + address + point.latitude + point.longitude).hashCode()

                SearchAddress(
                    id = id,
                    name = name,
                    address = address,
                    distance = distance,
                    rating = rating,
                    feedbacks = feedbacks,
                    point = point,
                    geoObject = obj
                )
            }

            val boundingBox = response.metadata.boundingBox ?: return

            searchState.value = SearchState.Success(
                searchItems,
                zoomToSearchResult,
                boundingBox,
            )
        }

        override fun onSearchError(error: Error) {
            searchState.value = SearchState.Error
        }
    }

    private fun submitSearch(query: String, geometry: Geometry) {
        searchSession?.cancel()
        searchSession = searchManager.submit(
            query,
            geometry,
            SearchOptions().apply {
                resultPageSize = 32
            },
            searchSessionListener
        )
        searchState.value = SearchState.Loading
        zoomToSearchResult = true
    }
}
