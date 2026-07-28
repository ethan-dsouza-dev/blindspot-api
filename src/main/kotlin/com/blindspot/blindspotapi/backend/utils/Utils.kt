package com.blindspot.blindspotapi.backend.utils

import com.blindspot.blindspotapi.backend.routing.dto.GeoCoordinate
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_METERS * c
}

/**
 * Encodes a list of coordinates into a Google encoded-polyline algorithm string (precision 5).
 * See: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
fun encodePolyline(coordinates: List<GeoCoordinate>): String {
    val result = StringBuilder()
    var prevLat = 0L
    var prevLng = 0L

    for (coordinate in coordinates) {
        val lat = round(coordinate.lat * 1e5).toLong()
        val lng = round(coordinate.lon * 1e5).toLong()

        encodeValue(lat - prevLat, result)
        encodeValue(lng - prevLng, result)

        prevLat = lat
        prevLng = lng
    }

    return result.toString()
}

private fun encodeValue(value: Long, result: StringBuilder) {
    var shifted = value shl 1
    if (value < 0) {
        shifted = shifted.inv()
    }

    while (shifted >= 0x20L) {
        result.append(((0x20L or (shifted and 0x1fL)) + 63L).toInt().toChar())
        shifted = shifted shr 5
    }
    result.append((shifted + 63L).toInt().toChar())
}