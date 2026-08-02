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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class PlaceServiceTest {

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
            types = listOf("bar", "night_club"),
        )
        val near = PlaceResult(
            id = "near",
            displayName = LocalizedText(text = "Near Bar"),
            location = LatLng(latitude = 37.7750, longitude = -122.4194),
            rating = 4.8,
            priceLevel = "PRICE_LEVEL_MODERATE",
            editorialSummary = LocalizedText(text = "Right around the corner"),
            types = listOf("bar"),
        )

        `when`(client.searchNearbyPlaces(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(far, near)))

        val result = service.findNearbyBars(originLat, originLng, 1500.0)

        assertEquals(listOf("near", "far"), result.map { it.id })
        assertTrue(result[0].distanceMeters < result[1].distanceMeters)

        val nearBar = result[0]
        assertEquals("Near Bar", nearBar.name)
        assertEquals(4.8, nearBar.rating)
        assertEquals(2, nearBar.priceLevel)
        assertEquals("bar", nearBar.category)
        assertEquals("Right around the corner", nearBar.description)
    }

    @Test
    fun `drops places without a location`() {
        val noLocation = PlaceResult(
            id = "no-loc",
            displayName = LocalizedText(text = "Ghost Bar"),
            location = null,
        )

        `when`(client.searchNearbyPlaces(originLat, originLng, 1500.0))
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

        `when`(client.searchNearbyPlaces(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(sparse)))

        val bar = service.findNearbyBars(originLat, originLng, 1500.0).single()

        assertEquals("Unknown", bar.name)
        assertNull(bar.rating)
        assertNull(bar.priceLevel)
        assertEquals("bar", bar.category)
        assertNull(bar.description)
    }

    @Test
    fun `filters places by price level`() {
        val cheap = PlaceResult(
            id = "cheap",
            displayName = LocalizedText(text = "Cheap Bar"),
            location = LatLng(latitude = 37.7750, longitude = -122.4194),
            priceLevel = "PRICE_LEVEL_INEXPENSIVE",
            types = listOf("bar"),
        )
        val pricey = PlaceResult(
            id = "pricey",
            displayName = LocalizedText(text = "Pricey Bar"),
            location = LatLng(latitude = 37.8049, longitude = -122.4194),
            priceLevel = "PRICE_LEVEL_EXPENSIVE",
            types = listOf("bar"),
        )

        `when`(client.searchNearbyPlaces(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(pricey, cheap)))

        val result = service.findNearbyBars(originLat, originLng, 1500.0, priceLevel = 1)

        assertEquals(listOf("cheap"), result.map { it.id })
    }

    @Test
    fun `does not filter when price level is omitted`() {
        val cheap = PlaceResult(
            id = "cheap",
            displayName = LocalizedText(text = "Cheap Bar"),
            location = LatLng(latitude = 37.7750, longitude = -122.4194),
            priceLevel = "PRICE_LEVEL_INEXPENSIVE",
            types = listOf("bar"),
        )
        val pricey = PlaceResult(
            id = "pricey",
            displayName = LocalizedText(text = "Pricey Bar"),
            location = LatLng(latitude = 37.8049, longitude = -122.4194),
            priceLevel = "PRICE_LEVEL_EXPENSIVE",
            types = listOf("bar"),
        )

        `when`(client.searchNearbyPlaces(originLat, originLng, 1500.0))
            .thenReturn(SearchNearbyResponse(places = listOf(pricey, cheap)))

        val result = service.findNearbyBars(originLat, originLng, 1500.0)

        assertEquals(listOf("cheap", "pricey"), result.map { it.id })
    }

    @Test
    fun `trending sorts places by review count descending`() {
        val lowReviews = PlaceResult(
            id = "low",
            displayName = LocalizedText(text = "New Cafe"),
            location = LatLng(latitude = 37.8049, longitude = -122.4194),
            rating = 4.0,
            userRatingCount = 5,
            types = listOf("cafe"),
        )
        val highReviews = PlaceResult(
            id = "high",
            displayName = LocalizedText(text = "Popular Pub"),
            location = LatLng(latitude = 37.7750, longitude = -122.4194),
            rating = 4.5,
            userRatingCount = 500,
            types = listOf("pub"),
        )

        `when`(client.searchNearbyPlaces(eq(originLat), eq(originLng), eq(5000.0), any(), eq("DISTANCE")))
            .thenReturn(SearchNearbyResponse(places = listOf(lowReviews, highReviews)))

        val result = service.findTrendingPlaces(originLat, originLng, 5000.0)

        assertEquals(listOf("high", "low"), result.map { it.id })
        assertEquals(500, result[0].reviewCount)
        assertEquals(5, result[1].reviewCount)
    }

    @Test
    fun `trending treats missing review count as zero so it sorts last`() {
        val noReviews = PlaceResult(
            id = "no-reviews",
            displayName = LocalizedText(text = "Hidden Spot"),
            location = LatLng(latitude = 37.8049, longitude = -122.4194),
            userRatingCount = null,
            types = listOf("bar"),
        )
        val withReviews = PlaceResult(
            id = "reviewed",
            displayName = LocalizedText(text = "Busy Bar"),
            location = LatLng(latitude = 37.7750, longitude = -122.4194),
            userRatingCount = 42,
            types = listOf("bar"),
        )

        `when`(client.searchNearbyPlaces(eq(originLat), eq(originLng), eq(5000.0), any(), eq("DISTANCE")))
            .thenReturn(SearchNearbyResponse(places = listOf(noReviews, withReviews)))

        val result = service.findTrendingPlaces(originLat, originLng, 5000.0)

        assertEquals(listOf("reviewed", "no-reviews"), result.map { it.id })
        assertEquals(42, result[0].reviewCount)
        assertNull(result[1].reviewCount)
    }

    @Test
    fun `trending calls the places client with the trending type list`() {
        val single = PlaceResult(
            id = "only",
            displayName = LocalizedText(text = "Solo Bar"),
            location = LatLng(latitude = 37.7749, longitude = -122.4194),
            userRatingCount = 10,
            types = listOf("bar"),
        )

        `when`(client.searchNearbyPlaces(eq(originLat), eq(originLng), eq(5000.0), any(), eq("DISTANCE")))
            .thenReturn(SearchNearbyResponse(places = listOf(single)))

        service.findTrendingPlaces(originLat, originLng, 5000.0)

        verify(client).searchNearbyPlaces(
            eq(originLat), eq(originLng), eq(5000.0),
            eq(listOf("bar", "night_club", "restaurant", "cafe", "pub")),
            eq("DISTANCE"),
        )
    }
}
