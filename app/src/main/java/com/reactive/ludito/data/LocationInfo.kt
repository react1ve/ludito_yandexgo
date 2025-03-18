package com.reactive.ludito.data

data class LocationInfo(
    val name: String,
    val address: String = "",
    val rating: Int = 0,
    val feedbackCount: Int = 0,
)
