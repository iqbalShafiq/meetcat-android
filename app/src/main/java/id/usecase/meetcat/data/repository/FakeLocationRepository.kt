package id.usecase.meetcat.data.repository

import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.repository.LocationRepository
import kotlinx.coroutines.delay

class FakeLocationRepository : LocationRepository {

    // Default fake location (Jakarta, Indonesia)
    private val fakeLocation = Location(
        latitude = -6.2088,
        longitude = 106.8456,
        address = "Jakarta, Indonesia",
        name = "Jakarta"
    )

    private var hasPermission = false

    override suspend fun getCurrentLocation(): Result<Location> {
        delay(1000) // Simulate network/GPS delay

        return if (hasPermission) {
            Result.success(fakeLocation)
        } else {
            Result.failure(SecurityException("Location permission not granted"))
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        return hasPermission
    }

    override fun updatePermissionStatus(granted: Boolean) {
        hasPermission = granted
    }
}
