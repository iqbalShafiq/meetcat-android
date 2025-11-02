package id.usecase.meetcat.presentation.util

import id.usecase.meetcat.domain.model.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculate distance between two locations using Haversine formula
 * @return distance in meters
 */
fun calculateDistance(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Float {
    val earthRadiusKm = 6371.0

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadiusKm * c * 1000).toFloat() // Convert to meters
}

/**
 * Calculate distance between two locations
 * @return distance in meters
 */
fun calculateDistance(from: Location, to: Location): Float {
    return calculateDistance(
        from.latitude,
        from.longitude,
        to.latitude,
        to.longitude
    )
}
