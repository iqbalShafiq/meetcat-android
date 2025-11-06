package id.usecase.meetcat.presentation.screen.followinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowingListViewModel(
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowingListUiState(userId = userId))
    val uiState: StateFlow<FollowingListUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<FollowingListUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadFollowing()
    }

    fun onEvent(event: FollowingListUiEvent) {
        when (event) {
            is FollowingListUiEvent.Refresh -> {
                loadFollowing(isRefresh = true)
            }

            is FollowingListUiEvent.LoadMore -> {
                // Mock pagination - In production, this would load more following when user scrolls to bottom
                // See UserProfileViewModel.loadMore() for detailed implementation example
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

    private fun loadFollowing(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            // Simulate loading
            delay(1000)

            // Mock following data
            val mockFollowing = listOf(
                User(
                    id = "following1",
                    username = "cat_photos",
                    displayName = "Cat Photos",
                    profileImageUrl = null,
                    bio = "Best cat photos",
                    followersCount = 500,
                    followingCount = 234,
                    postsCount = 120,
                    isFollowing = true
                ),
                User(
                    id = "following2",
                    username = "cute_cats",
                    displayName = "Cute Cats",
                    profileImageUrl = null,
                    bio = "Cutest cats ever",
                    followersCount = 789,
                    followingCount = 345,
                    postsCount = 234,
                    isFollowing = true
                ),
                User(
                    id = "following3",
                    username = "cat_memes",
                    displayName = "Cat Memes",
                    profileImageUrl = null,
                    bio = "Daily cat memes",
                    followersCount = 1200,
                    followingCount = 456,
                    postsCount = 456,
                    isFollowing = true
                )
            )

            _uiState.update {
                it.copy(
                    following = mockFollowing.toImmutableList(),
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    private fun toggleFollow(userId: String) {
        viewModelScope.launch {
            val updatedFollowing = _uiState.value.following.map { user ->
                if (user.id == userId) {
                    user.copy(isFollowing = !user.isFollowing)
                } else {
                    user
                }
            }.toImmutableList()

            _uiState.update { it.copy(following = updatedFollowing) }
        }
    }
}
