package id.usecase.meetcat.presentation.screen.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.repository.LocationRepository
import id.usecase.meetcat.domain.usecase.location.GetCurrentLocationUseCase
import id.usecase.meetcat.domain.usecase.location.HasLocationPermissionUseCase
import id.usecase.meetcat.domain.usecase.post.GetNearbyPostsUseCase
import id.usecase.meetcat.domain.usecase.post.LovePostUseCase
import id.usecase.meetcat.domain.usecase.post.UnlovePostUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapsViewModel(
    private val locationRepository: LocationRepository,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val hasLocationPermissionUseCase: HasLocationPermissionUseCase,
    private val getNearbyPostsUseCase: GetNearbyPostsUseCase,
    private val lovePostUseCase: LovePostUseCase,
    private val unlovePostUseCase: UnlovePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MapsUiEffect>()
    val uiEffect: Flow<MapsUiEffect> = _uiEffect.receiveAsFlow()

    init {
        checkLocationPermission()
    }

    fun onEvent(event: MapsUiEvent) {
        when (event) {
            is MapsUiEvent.RequestLocationPermission -> requestLocationPermission()
            is MapsUiEvent.LoadCurrentLocation -> loadCurrentLocation()
            is MapsUiEvent.LoadNearbyPosts -> loadNearbyPosts()
            is MapsUiEvent.Refresh -> refresh()
            is MapsUiEvent.MarkerClick -> selectMarker(event.postId)
            is MapsUiEvent.DismissMarkerDetail -> dismissMarkerDetail()
            is MapsUiEvent.LovePost -> toggleLovePost(event.postId)
            is MapsUiEvent.NavigateToPost -> {
                viewModelScope.launch {
                    _uiEffect.send(MapsUiEffect.NavigateToPost(event.postId))
                }
            }
            is MapsUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(MapsUiEffect.NavigateToProfile(event.userId))
                }
            }
        }
    }

    private fun checkLocationPermission() {
        viewModelScope.launch {
            val hasPermission = hasLocationPermissionUseCase()
            _uiState.update { it.copy(hasLocationPermission = hasPermission) }

            if (hasPermission) {
                loadCurrentLocation()
            }
        }
    }

    private fun requestLocationPermission() {
        viewModelScope.launch {
            _uiEffect.send(MapsUiEffect.RequestLocationPermission)
        }
    }

    private fun loadCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }

            val result = getCurrentLocationUseCase()

            result.fold(
                onSuccess = { location ->
                    _uiState.update {
                        it.copy(
                            currentLocation = location,
                            isLoadingLocation = false,
                            error = null
                        )
                    }
                    loadNearbyPosts()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingLocation = false,
                            error = error.message ?: "Failed to get location"
                        )
                    }
                    _uiEffect.send(
                        MapsUiEffect.ShowError(error.message ?: "Failed to get location")
                    )
                }
            )
        }
    }

    private fun loadNearbyPosts() {
        val currentLocation = _uiState.value.currentLocation ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = getNearbyPostsUseCase(currentLocation)

            result.fold(
                onSuccess = { posts ->
                    _uiState.update {
                        it.copy(
                            nearbyPosts = posts.toImmutableList(),
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load nearby posts"
                        )
                    }
                    _uiEffect.send(
                        MapsUiEffect.ShowError(error.message ?: "Failed to load nearby posts")
                    )
                }
            )
        }
    }

    private fun refresh() {
        checkLocationPermission()
    }

    private fun selectMarker(postId: String) {
        _uiState.update { it.copy(selectedPostId = postId) }
    }

    private fun dismissMarkerDetail() {
        _uiState.update { it.copy(selectedPostId = null) }
    }

    private fun toggleLovePost(postId: String) {
        viewModelScope.launch {
            // Optimistically update UI
            _uiState.update { state ->
                state.copy(
                    nearbyPosts = state.nearbyPosts.map { post ->
                        if (post.id == postId) {
                            post.copy(
                                isLoved = !post.isLoved,
                                lovesCount = if (post.isLoved)
                                    post.lovesCount - 1
                                else
                                    post.lovesCount + 1
                            )
                        } else post
                    }.toImmutableList()
                )
            }

            val currentPost = _uiState.value.nearbyPosts.find { it.id == postId }

            val result = if (currentPost?.isLoved == true) {
                unlovePostUseCase(postId)
            } else {
                lovePostUseCase(postId)
            }

            result.onFailure {
                // Revert on failure
                _uiState.update { state ->
                    state.copy(
                        nearbyPosts = state.nearbyPosts.map { post ->
                            if (post.id == postId) {
                                post.copy(
                                    isLoved = !post.isLoved,
                                    lovesCount = if (post.isLoved)
                                        post.lovesCount - 1
                                    else
                                        post.lovesCount + 1
                                )
                            } else post
                        }.toImmutableList()
                    )
                }
                _uiEffect.send(MapsUiEffect.ShowError("Failed to love post"))
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        // Update repository permission status first
        locationRepository.updatePermissionStatus(granted)

        // Then update UI state
        _uiState.update { it.copy(hasLocationPermission = granted) }

        if (granted) {
            loadCurrentLocation()
        } else {
            viewModelScope.launch {
                _uiEffect.send(MapsUiEffect.ShowLocationPermissionRationale)
            }
        }
    }
}
