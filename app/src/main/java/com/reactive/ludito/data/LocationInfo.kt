package com.reactive.ludito.data

data class LocationInfo(
    val name: String,
    val address: String = "",
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Float = 0f,
    val feedbackCount: Int = 0
)
