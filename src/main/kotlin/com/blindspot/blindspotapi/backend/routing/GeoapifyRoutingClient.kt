package com.blindspot.blindspotapi.backend.routing

import com.blindspot.blindspotapi.backend.config.GeoapifyRoutingProperties
import com.blindspot.blindspotapi.backend.routing.dto.GeoapifyRouteResult
import com.blindspot.blindspotapi.backend.routing.dto.GeoapifyRoutingResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class GeoapifyRoutingClient(
    private val geoapifyRoutingRestClient: RestClient,
    private val properties: GeoapifyRoutingProperties,
) {

    companion object {
        private const val ROUTING_PATH = "/v1/routing"
        private const val MODE = "walk"
        private const val FORMAT = "json"
    }

    fun getRoute(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double,
    ): GeoapifyRouteResult? {
        val waypoints = "$originLat,$originLng|$destLat,$destLng"

        val response = geoapifyRoutingRestClient.get()
            .uri { builder ->
                builder.path(ROUTING_PATH)
                    .queryParam("waypoints", waypoints)
                    .queryParam("mode", MODE)
                    .queryParam("format", FORMAT)
                    .queryParam("apiKey", properties.apiKey)
                    .build()
            }
            .retrieve()
            .body<GeoapifyRoutingResponse>()

        return response?.results?.firstOrNull()
    }
}
