package com.blindspot.blindspotapi.backend.places.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SearchNearbyRequest(
    val includedTypes: List<String>,
    val maxResultCount: Int,
    val rankPreference: String,
    val locationRestriction: LocationRestriction,
    val priceLevels: List<String>? = null,
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
    val types: List<String>? = null,
    val photos: List<Photo>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LocalizedText(
    val text: String? = null,
    val languageCode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Photo(
    val name: String? = null,
    val heightPx: Int? = null,
    val widthPx: Int? = null,
    val authorAttributions: List<AuthorAttribution>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AuthorAttribution(
    val displayName: String? = null,
    val uri: String? = null,
    val photoUri: String? = null,
)
