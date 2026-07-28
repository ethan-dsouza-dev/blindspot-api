package com.blindspot.blindspotapi.backend.routing

import com.blindspot.blindspotapi.backend.routing.dto.RouteResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/routing")
class RoutingController(
    private val routingService: RoutingService,
) {

    @GetMapping("/route")
    fun route(
        @RequestParam originLat: Double,
        @RequestParam originLng: Double,
        @RequestParam destLat: Double,
        @RequestParam destLng: Double,
        @RequestParam(required = false, defaultValue = "walk") mode: String,
    ): RouteResponse {
        validateLatLng(originLat, originLng)
        validateLatLng(destLat, destLng)

        return routingService.getRoute(originLat, originLng, destLat, destLng, mode)
    }

    private fun validateLatLng(lat: Double, lng: Double) {
        if (lat < -90.0 || lat > 90.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "lat must be between -90 and 90")
        }
        if (lng < -180.0 || lng > 180.0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "lng must be between -180 and 180")
        }
    }
}
