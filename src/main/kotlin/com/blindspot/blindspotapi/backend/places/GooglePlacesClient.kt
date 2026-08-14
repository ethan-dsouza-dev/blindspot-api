package com.blindspot.blindspotapi.backend.places

import com.blindspot.blindspotapi.backend.config.GooglePlacesProperties
import com.blindspot.blindspotapi.backend.places.dto.Circle
import com.blindspot.blindspotapi.backend.places.dto.LatLng
import com.blindspot.blindspotapi.backend.places.dto.LocationRestriction
import com.blindspot.blindspotapi.backend.places.dto.PlaceResult
import com.blindspot.blindspotapi.backend.places.dto.SearchNearbyRequest
import com.blindspot.blindspotapi.backend.places.dto.SearchNearbyResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GooglePlacesClient(
    private val googlePlacesRestClient: RestClient,
    private val properties: GooglePlacesProperties,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val SEARCH_NEARBY_PATH = "/v1/places:searchNearby"

        // Nearby Search wraps results in a "places" array, so each field needs that prefix.
        private const val SEARCH_FIELD_MASK =
            "places.id,places.displayName,places.location,places.rating,places.priceLevel,places.userRatingCount,places.editorialSummary,places.types,places.photos"

        // Place Details returns a single object directly (no "places" wrapper), so field names
        // must NOT be prefixed — using SEARCH_FIELD_MASK here causes a 400 INVALID_ARGUMENT from
        // Google ("Cannot find matching fields for path 'places.id'").
        private const val DETAILS_FIELD_MASK =
            "id,displayName,location,rating,priceLevel,userRatingCount,editorialSummary,types,photos"

        private const val MAX_RESULT_COUNT = 20
    }

    fun searchNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        types: List<String> = listOf("bar"),
        rankPreference: String = "POPULARITY",
    ): SearchNearbyResponse {
        val request = SearchNearbyRequest(
            includedTypes = types,
            maxResultCount = MAX_RESULT_COUNT,
            rankPreference = rankPreference,
            locationRestriction = LocationRestriction(
                circle = Circle(
                    center = LatLng(latitude = latitude, longitude = longitude),
                    radius = radiusMeters,
                ),
            ),
        )

        return googlePlacesRestClient.post()
            .uri(SEARCH_NEARBY_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Goog-Api-Key", properties.apiKey)
            .header("X-Goog-FieldMask", SEARCH_FIELD_MASK)
            .body(request)
            .retrieve()
            .body<SearchNearbyResponse>()
            ?: SearchNearbyResponse()
    }

    fun getPlaceDetails(placeId: String): PlaceResult? {
        return try {
            googlePlacesRestClient.get()
                .uri("/v1/places/{placeId}", placeId)
                .header("X-Goog-Api-Key", properties.apiKey)
                .header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
                .retrieve()
                .body<PlaceResult>()
        } catch (e: Exception) {
            logger.warn("Failed to fetch place details for placeId={}: {}", placeId, e.message)
            null
        }
    }
}