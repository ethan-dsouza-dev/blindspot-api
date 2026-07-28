package com.blindspot.blindspotapi.backend.bars

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(PlacesNearbyController::class)
class PlaceControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var barService: BarService

    @Test
    fun `returns nearby bars as json`() {
        whenever(barService.findNearbyBars(eq(37.7749), eq(-122.4194), eq(1500.0), isNull()))
            .thenReturn(
                listOf(
                    Place(
                        id = "near",
                        name = "Near Bar",
                        latitude = 37.775,
                        longitude = -122.4194,
                        description = "Around the corner",
                        rating = 4.8,
                        priceLevel = 2,
                        distanceMeters = 11.1,
                    ),
                ),
            )

        mockMvc.get("/bars/nearby") {
            param("lat", "37.7749")
            param("lng", "-122.4194")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].id") { value("near") }
            jsonPath("$[0].name") { value("Near Bar") }
            jsonPath("$[0].price_level") { value(2) }
            jsonPath("$[0].distance_meters") { value(11.1) }
        }
    }

    @Test
    fun `uses default radius when not provided`() {
        whenever(barService.findNearbyBars(any(), any(), eq(1500.0), isNull()))
            .thenReturn(emptyList())

        mockMvc.get("/bars/nearby") {
            param("lat", "37.7749")
            param("lng", "-122.4194")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `returns 400 when lat is missing`() {
        mockMvc.get("/bars/nearby") {
            param("lng", "-122.4194")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `returns 400 when lat is out of range`() {
        mockMvc.get("/bars/nearby") {
            param("lat", "100.0")
            param("lng", "-122.4194")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `returns 400 when radius is not positive`() {
        mockMvc.get("/bars/nearby") {
            param("lat", "37.7749")
            param("lng", "-122.4194")
            param("radius", "0")
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
