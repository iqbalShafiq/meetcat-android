package id.usecase.meetcat.domain.repository

import id.usecase.meetcat.domain.model.Location

interface LocationRepository {
    /**
     * Get current device location
     * @return Result containing current Location or error
     */
    suspend fun getCurrentLocation(): Result<Location>

    /**
     * Check if location permission is granted
     */
    suspend fun hasLocationPermission(): Boolean

    /**
     * Update location permission status
     * Should be called after user grants/denies permission
     */
    fun updatePermissionStatus(granted: Boolean)
}
