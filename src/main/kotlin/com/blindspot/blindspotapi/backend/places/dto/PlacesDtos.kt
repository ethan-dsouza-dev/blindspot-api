package com.blindspot.blindspotapi.backend.places.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class SearchNearbyRequest(
    val includedTypes: List<String>,
    val maxResultCount: Int,
    val rankPreference: String,
    val locationRestriction: LocationRestriction,
)

data class LocationRestriction(
    val circle: Circle,
)

data class Circle(
    val center: LatLng,
    val radius: Double,
)

data class LatLng(
    val latitude: Double,
    val longitude: Double,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchNearbyResponse(
    val places: List<PlaceResult> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlaceResult(
    val id: String,
    val displayName: LocalizedText? = null,
    val location: LatLng? = null,
    val rating: Double? = null,
    val priceLevel: String? = null,
    val editorialSummary: LocalizedText? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocalizedText(
    val text: String? = null,
    val languageCode: String? = null,
)
