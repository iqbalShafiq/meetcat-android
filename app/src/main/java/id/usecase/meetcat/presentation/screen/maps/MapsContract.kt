package id.usecase.meetcat.presentation.screen.maps

import androidx.compose.runtime.Immutable
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.Post
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MapsUiState(
    val nearbyPosts: ImmutableList<Post> = persistentListOf(),
    val currentLocation: Location? = null,
    val selectedPostId: String? = null,
    val isLoading: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val error: String? = null
)

sealed class MapsUiEvent {
    data object RequestLocationPermission : MapsUiEvent()
    data object LoadCurrentLocation : MapsUiEvent()
    data object LoadNearbyPosts : MapsUiEvent()
    data object Refresh : MapsUiEvent()
    data class MarkerClick(val postId: String) : MapsUiEvent()
    data object DismissMarkerDetail : MapsUiEvent()
    data class LovePost(val postId: String) : MapsUiEvent()
    data class NavigateToPost(val postId: String) : MapsUiEvent()
    data class NavigateToProfile(val userId: String) : MapsUiEvent()
}

sealed class MapsUiEffect {
    data object RequestLocationPermission : MapsUiEffect()
    data object ShowLocationPermissionRationale : MapsUiEffect()
    data class NavigateToPost(val postId: String) : MapsUiEffect()
    data class NavigateToProfile(val userId: String) : MapsUiEffect()
    data class ShowError(val message: String) : MapsUiEffect()
}
