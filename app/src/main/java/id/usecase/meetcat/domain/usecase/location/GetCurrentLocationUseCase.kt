package id.usecase.meetcat.domain.usecase.location

import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.repository.LocationRepository

class GetCurrentLocationUseCase(
    private val locationRepository: LocationRepository
) {
    /**
     * Get current device location
     * @return Result containing current Location or error if location unavailable
     */
    suspend operator fun invoke(): Result<Location> {
        return locationRepository.getCurrentLocation()
    }
}
