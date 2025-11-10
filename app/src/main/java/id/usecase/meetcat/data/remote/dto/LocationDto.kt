package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.Location
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val name: String? = null
)

fun LocationDto.toDomain(): Location {
    return Location(
        latitude = latitude,
        longitude = longitude,
        address = address,
        name = name
    )
}

fun Location.toDto(): LocationDto {
    return LocationDto(
        latitude = latitude,
        longitude = longitude,
        address = address,
        name = name
    )
}
