package com.reactive.ludito.ui.screens.tabs.map.search

import androidx.lifecycle.viewModelScope
import com.reactive.ludito.data.SearchAddress
import com.reactive.premier.base.BasePremierViewModel
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
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
    private val userLocation = MutableStateFlow<Point?>(null)
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
        userLocation
    ) { query, searchState, userLocation ->
        MapUiState(
            query = query,
            searchState = searchState,
            userLocation = userLocation
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, MapUiState())

    fun setQueryText(value: String) {
        query.value = value
    }

    fun setVisibleRegion(region: VisibleRegion) {
        this.region.value = region
    }

    fun setUserLocation(location: Point) {
        userLocation.value = location
    }

    fun startSearch(searchText: String? = null) {
        val text = searchText ?: query.value
        if (text.isEmpty()) return
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
                try {
                    val obj = searchResult.obj ?: return@mapNotNull null
                    val point = obj.geometry.firstOrNull()?.point ?: return@mapNotNull null

                    val name = obj.name ?: ""
                    val address = obj.descriptionText ?: ""

                    var distance = ""
                    var rating = 0
                    var feedbacks = 0

                    extractMetadata(obj)?.let { metadata ->
                        distance = metadata.distance
                        rating = metadata.rating
                        feedbacks = metadata.feedbacks
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
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

    private fun extractMetadata(obj: GeoObject): MetadataResult? {
        return try {
            var distance = ""
            var rating = 0
            var feedbacks = 0

            obj.metadataContainer?.let { container ->
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
                }

                try {
                    val businessMetadata =
                        container.getItem(com.yandex.mapkit.search.BusinessObjectMetadata::class.java)
                    if (businessMetadata != null) {
                        extractNumberField(businessMetadata, "rating")?.let {
                            rating = it
                        } ?: extractNumberField(businessMetadata, "score")?.let {
                            rating = it
                        }

                        extractNumberField(businessMetadata, "reviewCount")?.let {
                            feedbacks = it
                        } ?: extractNumberField(businessMetadata, "reviews")?.let {
                            feedbacks = it
                        }
                    }
                } catch (e: Exception) {
                }
            }

            MetadataResult(distance, rating, feedbacks)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractNumberField(obj: Any, fieldName: String): Int? {
        return try {
            obj::class.java.getDeclaredField(fieldName)?.let { field ->
                field.isAccessible = true
                val value = field.get(obj)
                if (value != null && value is Number) {
                    value.toInt()
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun submitSearch(query: String, geometry: Geometry) {
        searchSession?.cancel()
        searchSession = searchManager.submit(
            query,
            geometry,
            SearchOptions().apply {
                resultPageSize = 32
                userPosition = userLocation.value
            },
            searchSessionListener
        )
        searchState.value = SearchState.Loading
        zoomToSearchResult = true
    }

    private data class MetadataResult(
        val distance: String,
        val rating: Int,
        val feedbacks: Int
    )
}
