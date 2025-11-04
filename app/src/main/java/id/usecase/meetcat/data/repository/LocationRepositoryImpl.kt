package id.usecase.meetcat.data.repository

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationRepositoryImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<Location> {
        // Check if we have permission first
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val result = Location(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = null,
                            name = null
                        )
                        continuation.resume(Result.success(result))
                    } else {
                        continuation.resume(
                            Result.failure(Exception("Unable to get current location"))
                        )
                    }
                }.addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
            } catch (exception: Exception) {
                continuation.resume(Result.failure(exception))
            }
        }
    }

    override suspend fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return fineLocation == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                coarseLocation == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    override fun updatePermissionStatus(granted: Boolean) {
        // Permission status is managed by Android system
        // This method is kept for consistency but doesn't need to do anything
        // as actual permission is checked via ContextCompat.checkSelfPermission()
    }
}
