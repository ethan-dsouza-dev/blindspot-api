package com.blindspot.blindspotapi.backend.bars

import com.fasterxml.jackson.annotation.JsonProperty

data class Place(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("category")
    val category: String? = null,

    @JsonProperty("latitude")
    val latitude: Double,

    @JsonProperty("longitude")
    val longitude: Double,

    @JsonProperty("image_url")
    val imageUrl: String? = null,

    @JsonProperty("rating")
    val rating: Double? = null,

    @JsonProperty("price_level")
    val priceLevel: Int? = null,

    @JsonProperty("distance_meters")
    val distanceMeters: Double,
)
