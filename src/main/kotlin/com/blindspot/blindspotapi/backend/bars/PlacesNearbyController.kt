package com.blindspot.blindspotapi.backend.bars

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/bars")
class PlacesNearbyController(
    private val barService: BarService,
) {

    @GetMapping("/nearby")
    fun nearby(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "1500.0") radius: Double,
        @RequestParam(required = false) priceLevel: Int? = null,
    ): List<Place> {
        validateLatLng(lat, lng)
        validateRadius(radius)
        if (priceLevel != null && (priceLevel < 1 || priceLevel > 4)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "priceLevel must be between 1 and 4")
        }

        return barService.findNearbyBars(lat, lng, radius, priceLevel)
    }

    @GetMapping("/trending")
    fun trending(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
        @RequestParam(required = false, defaultValue = "5000.0") radius: Double,
    ): List<Place> {
        validateLatLng(lat, lng)
        validateRadius(radius)

        return barService.findTrendingPlaces(lat, lng, radius)
    }

    private fun validateLatLng(lat: Double, lng: Double) {
        if (lat < -90.0 || lat > 90.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "lat must be between -90 and 90")
        }
        if (lng < -180.0 || lng > 180.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "lng must be between -180 and 180")
        }
    }

    private fun validateRadius(radius: Double) {
        if (radius <= 0.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "radius must be greater than 0")
        }
        if (radius > 50_000.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "radius must not exceed 50000 metres")
        }
    }
}
