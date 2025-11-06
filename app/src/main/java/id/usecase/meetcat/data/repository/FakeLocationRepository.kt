package id.usecase.meetcat.data.repository

import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.repository.LocationRepository
import kotlinx.coroutines.delay

/**
 * Fake implementation of LocationRepository for testing and development.
 * Returns a default fake location (Jakarta, Indonesia).
 *
 * Features:
 * - No Android dependencies (fully unit testable)
 * - Simulates permission checking
 * - Test isolation via reset() method
 * - Configurable fake location
 */
class FakeLocationRepository : LocationRepository {

    // Default fake location (Jakarta, Indonesia)
    private var fakeLocation = Location(
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

    /**
     * Set custom fake location for testing.
     * Useful for testing location-based features.
     */
    fun setFakeLocation(location: Location) {
        fakeLocation = location
    }

    /**
     * Reset repository state for test isolation.
     * Call this between test cases to ensure clean state.
     */
    fun reset() {
        hasPermission = false
        fakeLocation = Location(
            latitude = -6.2088,
            longitude = 106.8456,
            address = "Jakarta, Indonesia",
            name = "Jakarta"
        )
    }
}
