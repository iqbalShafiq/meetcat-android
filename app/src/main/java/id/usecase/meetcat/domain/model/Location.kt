package id.usecase.meetcat.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val name: String?
)
