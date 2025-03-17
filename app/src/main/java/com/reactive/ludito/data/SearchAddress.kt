package com.reactive.ludito.data

import com.yandex.mapkit.geometry.Point

data class SearchAddress(
    val id: Int,
    val name: String,
    val address: String,
    val distance: String,
    val location: Point?
)