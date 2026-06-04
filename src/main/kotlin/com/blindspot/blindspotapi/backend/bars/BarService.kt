package com.blindspot.blindspotapi.backend.bars

import com.blindspot.blindspotapi.backend.places.GooglePlacesClient
import com.blindspot.blindspotapi.backend.places.dto.PlaceResult
import org.springframework.stereotype.Service
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class BarService(
    private val googlePlacesClient: GooglePlacesClient,
) {

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0

        private val PRICE_LEVELS = mapOf(
            "PRICE_LEVEL_FREE" to 0,
            "PRICE_LEVEL_INEXPENSIVE" to 1,
            "PRICE_LEVEL_MODERATE" to 2,
            "PRICE_LEVEL_EXPENSIVE" to 3,
            "PRICE_LEVEL_VERY_EXPENSIVE" to 4,
        )
    }

    fun findNearbyBars(latitude: Double, longitude: Double, radiusMeters: Double): List<Bar> {
        val response = googlePlacesClient.searchNearbyBars(latitude, longitude, radiusMeters)

        return response.places
            .mapNotNull { place -> toBar(place, latitude, longitude) }
            .sortedBy { it.distanceMeters }
    }

    private fun toBar(place: PlaceResult, originLat: Double, originLng: Double): Bar? {
        val location = place.location ?: return null

        return Bar(
            id = place.id,
            name = place.displayName?.text ?: "Unknown",
            latitude = location.latitude,
            longitude = location.longitude,
            description = place.editorialSummary?.text,
            rating = place.rating,
            pricePoint = place.priceLevel?.let { PRICE_LEVELS[it] },
            distanceMeters = haversineMeters(originLat, originLng, location.latitude, location.longitude),
        )
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
}
