package id.usecase.meetcat.presentation.screen.followerslist

import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface FollowersListUiEvent {
    data object Refresh : FollowersListUiEvent
    data object LoadMore : FollowersListUiEvent
    data class NavigateToProfile(val userId: String) : FollowersListUiEvent
    data class ToggleFollow(val userId: String) : FollowersListUiEvent
    data object NavigateBack : FollowersListUiEvent
}

data class FollowersListUiState(
    val userId: String = "",
    val followers: ImmutableList<User> = persistentListOf(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

sealed interface FollowersListUiEffect {
    data class NavigateToProfile(val userId: String) : FollowersListUiEffect
    data object NavigateBack : FollowersListUiEffect
    data class ShowError(val message: String) : FollowersListUiEffect
}
