package com.reactive.ludito.data

import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.geometry.Point

data class SearchAddress(
    val id: Int,
    val name: String,
    val address: String,
    val distance: String,
    val rating: Int,
    val feedbacks: Int,
    val point: Point,
    val geoObject: GeoObject?,
)