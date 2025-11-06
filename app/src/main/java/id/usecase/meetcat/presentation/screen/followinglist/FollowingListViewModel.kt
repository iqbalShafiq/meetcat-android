package id.usecase.meetcat.presentation.screen.followinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.paging.FollowingPagingSource
import id.usecase.meetcat.domain.repository.UserRepository
import id.usecase.meetcat.domain.usecase.user.GetFollowingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowingListViewModel(
    private val userId: String,
    private val getFollowingUseCase: GetFollowingUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiEffect = Channel<FollowingListUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    // Track follow toggles for optimistic UI updates
    private val _toggledFollows = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Paging3 Flow for infinite scroll
    private val pagingFlow: Flow<PagingData<User>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { FollowingPagingSource(userId, getFollowingUseCase) }
    ).flow.cachedIn(viewModelScope)

    // Combine paging data with follow toggles for optimistic UI updates
    val following: Flow<PagingData<User>> = combine(
        pagingFlow,
        _toggledFollows
    ) { pagingData, toggledFollows ->
        pagingData.map { user ->
            // If user has been toggled, apply the optimistic update
            toggledFollows[user.id]?.let { isFollowing ->
                user.copy(isFollowing = isFollowing)
            } ?: user
        }
    }

    fun onEvent(event: FollowingListUiEvent) {
        when (event) {
            is FollowingListUiEvent.Refresh -> {
                // Refresh is handled by LazyPagingItems.refresh() in UI layer
            }

            is FollowingListUiEvent.LoadMore -> {
                // LoadMore is handled automatically by Paging3
            }

            is FollowingListUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(FollowingListUiEffect.NavigateToProfile(event.userId))
                }
            }

            is FollowingListUiEvent.ToggleFollow -> {
                toggleFollow(event.userId)
            }

            is FollowingListUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(FollowingListUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            // Get current state
            val currentToggles = _toggledFollows.value
            val currentFollowState = currentToggles[targetUserId]

            // Determine new follow state (optimistic update)
            val newFollowState = when (currentFollowState) {
                null -> false // Not toggled yet, assume was true (following), toggle to false
                true -> false
                false -> true
            }

            // Apply optimistic update
            _toggledFollows.update { current ->
                current + (targetUserId to newFollowState)
            }

            // Make API call
            val result = if (newFollowState) {
                userRepository.followUser(targetUserId)
            } else {
                userRepository.unfollowUser(targetUserId)
            }

            // Revert on failure
            result.onFailure {
                _toggledFollows.update { current ->
                    if (currentFollowState == null) {
                        current - targetUserId
                    } else {
                        current + (targetUserId to currentFollowState)
                    }
                }
                _uiEffect.send(FollowingListUiEffect.ShowError("Failed to update follow status"))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10
    }
}
