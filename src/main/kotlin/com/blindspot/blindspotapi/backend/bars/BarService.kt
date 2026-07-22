package com.blindspot.blindspotapi.backend.bars

import com.blindspot.blindspotapi.backend.places.GooglePlacesClient
import com.blindspot.blindspotapi.backend.places.dto.PlaceResult
import com.blindspot.blindspotapi.backend.utils.haversineMeters
import org.springframework.stereotype.Service

@Service
class BarService(
    private val googlePlacesClient: GooglePlacesClient,
) {

    companion object {
        private val PRICE_LEVELS = mapOf(
            "PRICE_LEVEL_FREE" to 0,
            "PRICE_LEVEL_INEXPENSIVE" to 1,
            "PRICE_LEVEL_MODERATE" to 2,
            "PRICE_LEVEL_EXPENSIVE" to 3,
            "PRICE_LEVEL_VERY_EXPENSIVE" to 4,
        )

        private val PRICE_LEVEL_NAMES = PRICE_LEVELS.entries.associate { (name, level) -> level to name }
    }

    fun findNearbyBars(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double,
        priceLevel: Int? = null,
    ): List<Place> {
        val priceLevelNames = priceLevel
            ?.let { PRICE_LEVEL_NAMES[it] }
            ?.let { listOf(it) }

        val response = googlePlacesClient.searchNearbyBars(latitude, longitude, radiusMeters, priceLevelNames)

        return response.places
            .mapNotNull { place -> toBar(place, latitude, longitude) }
            .sortedBy { it.distanceMeters }
    }

    private fun toBar(place: PlaceResult, originLat: Double, originLng: Double): Place? {
        val location = place.location ?: return null

        return Place(
            id = place.id,
            name = place.displayName?.text ?: "Unknown",
            description = place.editorialSummary?.text,
            category = place.types?.firstOrNull() ?: "",
            latitude = location.latitude,
            longitude = location.longitude,
            imageUrl = null,
            rating = place.rating,
            priceLevel = place.priceLevel?.let { PRICE_LEVELS[it] },
            distanceMeters = haversineMeters(originLat, originLng, location.latitude, location.longitude),
        )
    }
}
