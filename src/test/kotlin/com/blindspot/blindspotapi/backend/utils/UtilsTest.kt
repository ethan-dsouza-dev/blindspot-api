package com.blindspot.blindspotapi.backend.utils

import com.blindspot.blindspotapi.backend.routing.dto.GeoCoordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilsTest {

    @Test
    fun `encodePolyline matches the official Google polyline algorithm example`() {
        val coordinates = listOf(
            GeoCoordinate(lat = 38.5, lon = -120.2),
            GeoCoordinate(lat = 40.7, lon = -120.95),
            GeoCoordinate(lat = 43.252, lon = -126.453),
        )

        val encoded = encodePolyline(coordinates)

        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", encoded)
    }

    @Test
    fun `encodePolyline returns an empty string for no coordinates`() {
        assertEquals("", encodePolyline(emptyList()))
    }
}
