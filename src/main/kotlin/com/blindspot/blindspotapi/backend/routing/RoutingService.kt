package com.blindspot.blindspotapi.backend.routing

import com.blindspot.blindspotapi.backend.routing.dto.RouteResponse
import com.blindspot.blindspotapi.backend.utils.encodePolyline
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class RoutingService(
    private val geoapifyRoutingClient: GeoapifyRoutingClient,
) {

    fun getRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
    ): RouteResponse {
        val route = geoapifyRoutingClient.getRoute(originLat, originLng, destLat, destLng)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No route found")

        val coordinates = route.geometry.flatten()

        return RouteResponse(
            polyline = encodePolyline(coordinates),
            distanceMeters = route.distance,
            durationSeconds = route.time,
        )
    }
}
