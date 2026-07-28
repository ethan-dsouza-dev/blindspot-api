package com.blindspot.blindspotapi.backend.routing

import com.blindspot.blindspotapi.backend.routing.dto.GeoCoordinate
import com.blindspot.blindspotapi.backend.routing.dto.GeoapifyRouteResult
import com.blindspot.blindspotapi.backend.utils.encodePolyline
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.web.server.ResponseStatusException

class RoutingServiceTest {

    private val client = mock(GeoapifyRoutingClient::class.java)
    private val service = RoutingService(client)

    private val originLat = 37.7749
    private val originLng = -122.4194
    private val destLat = 37.7750
    private val destLng = -122.4180

    @Test
    fun `maps geoapify route into a route response with an encoded polyline`() {
        val leg = listOf(
            GeoCoordinate(lat = 38.5, lon = -120.2),
            GeoCoordinate(lat = 43.252, lon = -126.453),
        )
        `when`(client.getRoute(originLat, originLng, destLat, destLng, "walk"))
            .thenReturn(GeoapifyRouteResult(distance = 500.0, time = 120.0, geometry = listOf(leg)))

        val result = service.getRoute(originLat, originLng, destLat, destLng)

        assertEquals(encodePolyline(leg), result.polyline)
        assertEquals(500.0, result.distanceMeters)
        assertEquals(120.0, result.durationSeconds)
    }

    @Test
    fun `flattens multiple legs into a single polyline`() {
        val legOne = listOf(GeoCoordinate(lat = 38.5, lon = -120.2))
        val legTwo = listOf(GeoCoordinate(lat = 43.252, lon = -126.453))
        `when`(client.getRoute(originLat, originLng, destLat, destLng, "walk"))
            .thenReturn(GeoapifyRouteResult(distance = 500.0, time = 120.0, geometry = listOf(legOne, legTwo)))

        val result = service.getRoute(originLat, originLng, destLat, destLng)

        assertEquals(encodePolyline(legOne + legTwo), result.polyline)
    }

    @Test
    fun `throws not found when geoapify returns no route`() {
        `when`(client.getRoute(originLat, originLng, destLat, destLng, "walk")).thenReturn(null)

        assertThrows(ResponseStatusException::class.java) {
            service.getRoute(originLat, originLng, destLat, destLng)
        }
    }

    @Test
    fun `passes a custom mode through to the geoapify client`() {
        val leg = listOf(GeoCoordinate(lat = 38.5, lon = -120.2))
        `when`(client.getRoute(originLat, originLng, destLat, destLng, "drive"))
            .thenReturn(GeoapifyRouteResult(distance = 500.0, time = 120.0, geometry = listOf(leg)))

        val result = service.getRoute(originLat, originLng, destLat, destLng, mode = "drive")

        assertEquals(encodePolyline(leg), result.polyline)
    }
}
