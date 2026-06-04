package com.blindspot.blindspotapi.backend.bars

data class Bar(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String?,
    val rating: Double?,
    val pricePoint: Int?,
    val distanceMeters: Double,
)
