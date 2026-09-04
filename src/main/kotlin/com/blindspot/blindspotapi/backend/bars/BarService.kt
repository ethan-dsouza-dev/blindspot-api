package com.blindspot.blindspotapi.backend.bars

import com.blindspot.blindspotapi.backend.places.GooglePlacesClient
import com.blindspot.blindspotapi.backend.places.dto.PlaceResult
import com.blindspot.blindspotapi.backend.utils.haversineMeters
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Service
class BarService(
    private val googlePlacesClient: GooglePlacesClient,
) {

    // Bounded so a large ids list can't spawn unbounded threads; sized generously since these
    // are short-lived I/O-bound calls to Google, not CPU work.
    private val placeDetailsExecutor = Executors.newFixedThreadPool(10)

    companion object {
        private val PRICE_LEVELS = mapOf(
            "PRICE_LEVEL_FREE" to 0,
            "PRICE_LEVEL_INEXPENSIVE" to 1,
            "PRICE_LEVEL_MODERATE" to 2,
            "PRICE_LEVEL_EXPENSIVE" to 3,
            "PRICE_LEVEL_VERY_EXPENSIVE" to 4,
        )

        private val TRENDING_TYPES = listOf("bar", "night_club", "restaurant", "cafe", "pub")
    }

    fun findNearbyBars(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        priceLevel: Int? = null,
    ): List<Place> {
        val response = googlePlacesClient.searchNearbyPlaces(latitude, longitude, radiusMeters)

        return response.places
            .mapNotNull { place -> toPlace(place, latitude, longitude) }
            .filter { priceLevel == null || it.priceLevel == priceLevel }
            .sortedBy { it.distanceMeters }
    }

    fun findTrendingPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
    ): List<Place> {
        val response = googlePlacesClient.searchNearbyPlaces(latitude, longitude, radiusMeters, TRENDING_TYPES, "DISTANCE")

        return response.places
            .mapNotNull { place -> toPlace(place, latitude, longitude) }
            .sortedByDescending { it.reviewCount ?: 0 }
    }

    /** Fetches details for each id concurrently, so a favorites list of N places costs roughly
     * one round trip's worth of latency rather than N sequential ones. */
    fun findByIds(placeIds: List<String>): List<Place> {
        val futures = placeIds.map { id ->
            CompletableFuture.supplyAsync({ googlePlacesClient.getPlaceDetails(id) }, placeDetailsExecutor)
        }

        return futures
            .map { it.join() }
            .mapNotNull { result -> result?.let { toPlace(it, originLat = null, originLng = null) } }
    }

    private fun toPlace(place: PlaceResult, originLat: Double?, originLng: Double?): Place? {
        val location = place.location ?: return null

        return Place(
            id = place.id,
            name = place.displayName?.text ?: "Unknown",
            description = place.editorialSummary?.text,
            category = place.types?.firstOrNull() ?: "bar",
            latitude = location.latitude,
            longitude = location.longitude,
            imageUrl = null,
            rating = place.rating,
            priceLevel = place.priceLevel?.let { PRICE_LEVELS[it] },
            reviewCount = place.userRatingCount,
            distanceMeters = if (originLat != null && originLng != null) {
                haversineMeters(originLat, originLng, location.latitude, location.longitude)
            } else null,
        )
    }
}