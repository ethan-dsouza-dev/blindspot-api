package com.blindspot.blindspotapi.backend.bars

import com.blindspot.blindspotapi.backend.places.GooglePlacesClient
import com.blindspot.blindspotapi.backend.places.dto.LatLng
import com.blindspot.blindspotapi.backend.places.dto.LocalizedText
import com.blindspot.blindspotapi.backend.places.dto.PlaceResult
import com.blindspot.blindspotapi.backend.places.dto.SearchNearbyResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class BarServiceTest {

    private val client = mock(GooglePlacesClient::class.java)
    private val service = BarService(client)

    // Origin: roughly San Francisco
    private val originLat = 37.7749
    private val originLng = -122.4194

    @Test
    fun `maps places and sorts ascending by distance`() {
        val far = PlaceResult(
            id = "far",
            displayName = LocalizedText(text = "Far Bar"),
            location = LatLng(latitude = 37.8049, longitude = -122.4194),
            rating = 4.1,
            priceLevel = "PRICE_LEVEL_EXPENSIVE",
            editorialSummary = LocalizedText(text = "A distant watering hole"),
        )
        val near = PlaceResult(
            id = "near",
            displayName = LocalizedText(text = "Near Bar"),
            location = LatLng(latitude = 37.7750, longitude = -122.4194),
            rating = 4.8,
            priceLevel = "PRICE_LEVEL_MODERATE",
            editorialSummary = LocalizedText(text = "Right around the corner"),
        )

        `when`(client.searchNearbyBars(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(far, near)))

        val result = service.findNearbyBars(originLat, originLng, 1500.0)

        assertEquals(listOf("near", "far"), result.map { it.id })
        assertTrue(result[0].distanceMeters < result[1].distanceMeters)

        val nearBar = result[0]
        assertEquals("Near Bar", nearBar.name)
        assertEquals(4.8, nearBar.rating)
        assertEquals(2, nearBar.pricePoint)
        assertEquals("Right around the corner", nearBar.description)
    }

    @Test
    fun `drops places without a location`() {
        val noLocation = PlaceResult(
            id = "no-loc",
            displayName = LocalizedText(text = "Ghost Bar"),
            location = null,
        )

        `when`(client.searchNearbyBars(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(noLocation)))

        val result = service.findNearbyBars(originLat, originLng, 1500.0)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `handles missing optional fields`() {
        val sparse = PlaceResult(
            id = "sparse",
            displayName = null,
            location = LatLng(latitude = 37.7749, longitude = -122.4194),
            rating = null,
            priceLevel = null,
            editorialSummary = null,
        )

        `when`(client.searchNearbyBars(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(sparse)))

        val bar = service.findNearbyBars(originLat, originLng, 1500.0).single()

        assertEquals("Unknown", bar.name)
        assertNull(bar.rating)
        assertNull(bar.pricePoint)
        assertNull(bar.description)
    }
}
