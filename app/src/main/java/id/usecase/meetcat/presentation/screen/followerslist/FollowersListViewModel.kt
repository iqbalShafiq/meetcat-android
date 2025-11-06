package id.usecase.meetcat.presentation.screen.followerslist

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

class FollowersListViewModel(
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(FollowersListUiState(userId = userId))
    val uiState: StateFlow<FollowersListUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<FollowersListUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadFollowers()
    }

    fun onEvent(event: FollowersListUiEvent) {
        when (event) {
            is FollowersListUiEvent.Refresh -> {
                loadFollowers(isRefresh = true)
            }

            is FollowersListUiEvent.LoadMore -> {
                // Mock pagination - In production, this would load more followers when user scrolls to bottom
                // See UserProfileViewModel.loadMore() for detailed implementation example
            }

            is FollowersListUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(FollowersListUiEffect.NavigateToProfile(event.userId))
                }
            }

            is FollowersListUiEvent.ToggleFollow -> {
                toggleFollow(event.userId)
            }

            is FollowersListUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(FollowersListUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadFollowers(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(isLoading = true) }
            }

            // Simulate loading
            delay(1000)

            // Mock followers data
            val mockFollowers = listOf(
                User(
                    id = "follower1",
                    username = "cat_lover",
                    displayName = "Cat Lover",
                    profileImageUrl = null,
                    bio = "I love cats!",
                    followersCount = 150,
                    followingCount = 89,
                    postsCount = 45,
                    isFollowing = true
                ),
                User(
                    id = "follower2",
                    username = "kitty_fan",
                    displayName = "Kitty Fan",
                    profileImageUrl = null,
                    bio = "Cat enthusiast",
                    followersCount = 230,
                    followingCount = 156,
                    postsCount = 78,
                    isFollowing = false
                ),
                User(
                    id = "follower3",
                    username = "meow_meow",
                    displayName = "Meow Meow",
                    profileImageUrl = null,
                    bio = "Meow meow meow",
                    followersCount = 89,
                    followingCount = 67,
                    postsCount = 34,
                    isFollowing = true
                )
            )

            _uiState.update {
                it.copy(
                    followers = mockFollowers.toImmutableList(),
                    isLoading = false,
                    isRefreshing = false
                )
            }
        }
    }

    private fun toggleFollow(userId: String) {
        viewModelScope.launch {
            val updatedFollowers = _uiState.value.followers.map { user ->
                if (user.id == userId) {
                    user.copy(isFollowing = !user.isFollowing)
                } else {
                    user
                }
            }.toImmutableList()

            _uiState.update { it.copy(followers = updatedFollowers) }
        }
    }
}
