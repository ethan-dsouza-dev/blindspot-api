package com.blindspot.blindspotapi.backend.routing.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoapifyRoutingResponse(
    val results: List<GeoapifyRouteResult> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoapifyRouteResult(
    val distance: Double = 0.0,
    val time: Double = 0.0,
    val geometry: List<List<GeoCoordinate>> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeoCoordinate(
    val lat: Double,
    val lon: Double,
)

data class RouteResponse(
    @JsonProperty("polyline")
    val polyline: String,

    @JsonProperty("distance_meters")
    val distanceMeters: Double,

    @JsonProperty("duration_seconds")
    val durationSeconds: Double,
)
