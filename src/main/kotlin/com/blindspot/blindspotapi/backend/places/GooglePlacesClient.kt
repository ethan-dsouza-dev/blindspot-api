package com.blindspot.blindspotapi.backend.places

import com.blindspot.blindspotapi.backend.config.GooglePlacesProperties
import com.blindspot.blindspotapi.backend.places.dto.Circle
import com.blindspot.blindspotapi.backend.places.dto.LatLng
import com.blindspot.blindspotapi.backend.places.dto.LocationRestriction
import com.blindspot.blindspotapi.backend.places.dto.SearchNearbyRequest
import com.blindspot.blindspotapi.backend.places.dto.SearchNearbyResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GooglePlacesClient(
    private val googlePlacesRestClient: RestClient,
    private val properties: GooglePlacesProperties,
) {

    companion object {
        private const val SEARCH_NEARBY_PATH = "/v1/places:searchNearby"
        private const val FIELD_MASK =
            "places.id,places.displayName,places.location,places.rating,places.priceLevel,places.editorialSummary,places.types,places.photos"
        private const val MAX_RESULT_COUNT = 20
    }

    fun searchNearbyBars(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
    ): SearchNearbyResponse {
        val request = SearchNearbyRequest(
            includedTypes = listOf("bar"),
            maxResultCount = MAX_RESULT_COUNT,
            rankPreference = "POPULARITY",
            locationRestriction = LocationRestriction(
                circle = Circle(
                    center = LatLng(latitude = latitude, longitude = longitude),
                    radius = radiusMeters,
                ),
            ),
        )

        return googlePlacesRestClient.post()
            .uri { builder ->
                builder.path(SEARCH_NEARBY_PATH)
                    .queryParam("key", properties.apiKey)
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Goog-FieldMask", FIELD_MASK)
            .body(request)
            .retrieve()
            .body<SearchNearbyResponse>()
            ?: SearchNearbyResponse()
    }
}
