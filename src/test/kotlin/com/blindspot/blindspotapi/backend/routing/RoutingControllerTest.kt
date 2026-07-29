package com.blindspot.blindspotapi.backend.routing

import com.blindspot.blindspotapi.backend.routing.dto.RouteResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import com.blindspot.blindspotapi.backend.auth.JwtService

@WebMvcTest(RoutingController::class)
class RoutingControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var routingService: RoutingService

    @MockitoBean
    lateinit var jwtService: JwtService

    @Test
    fun `returns route as json`() {
        whenever(
            routingService.getRoute(eq(37.7749), eq(-122.4194), eq(37.7750), eq(-122.4180), eq("walk")),
        ).thenReturn(
            RouteResponse(
                polyline = "abc123",
                distanceMeters = 500.0,
                durationSeconds = 120.0,
            ),
        )

        mockMvc.get("/routing/route") {
            param("originLat", "37.7749")
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-122.4180")
        }.andExpect {
            status { isOk() }
            jsonPath("$.polyline") { value("abc123") }
            jsonPath("$.distance_meters") { value(500.0) }
            jsonPath("$.duration_seconds") { value(120.0) }
        }
    }

    @Test
    fun `returns 400 when originLat is missing`() {
        mockMvc.get("/routing/route") {
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-122.4180")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `returns 400 when originLat is out of range`() {
        mockMvc.get("/routing/route") {
            param("originLat", "100.0")
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-122.4180")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `returns 400 when destLng is out of range`() {
        mockMvc.get("/routing/route") {
            param("originLat", "37.7749")
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-200.0")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `delegates to routing service with all params`() {
        whenever(routingService.getRoute(any(), any(), any(), any(), any()))
            .thenReturn(RouteResponse(polyline = "", distanceMeters = 0.0, durationSeconds = 0.0))

        mockMvc.get("/routing/route") {
            param("originLat", "37.7749")
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-122.4180")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `defaults mode to walk when not provided`() {
        whenever(
            routingService.getRoute(eq(37.7749), eq(-122.4194), eq(37.7750), eq(-122.4180), eq("walk")),
        ).thenReturn(RouteResponse(polyline = "abc123", distanceMeters = 500.0, durationSeconds = 120.0))

        mockMvc.get("/routing/route") {
            param("originLat", "37.7749")
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-122.4180")
        }.andExpect {
            status { isOk() }
            jsonPath("$.polyline") { value("abc123") }
        }
    }

    @Test
    fun `passes a custom mode request param through to the service`() {
        whenever(
            routingService.getRoute(eq(37.7749), eq(-122.4194), eq(37.7750), eq(-122.4180), eq("drive")),
        ).thenReturn(RouteResponse(polyline = "xyz789", distanceMeters = 500.0, durationSeconds = 120.0))

        mockMvc.get("/routing/route") {
            param("originLat", "37.7749")
            param("originLng", "-122.4194")
            param("destLat", "37.7750")
            param("destLng", "-122.4180")
            param("mode", "drive")
        }.andExpect {
            status { isOk() }
            jsonPath("$.polyline") { value("xyz789") }
        }
    }
}
