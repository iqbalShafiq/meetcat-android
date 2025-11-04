package id.usecase.meetcat.domain.usecase.location

import id.usecase.meetcat.domain.repository.LocationRepository

class HasLocationPermissionUseCase(
    private val locationRepository: LocationRepository
) {
    /**
     * Check if location permission is granted
     * @return true if location permission is granted, false otherwise
     */
    suspend operator fun invoke(): Boolean {
        return locationRepository.hasLocationPermission()
    }
}
