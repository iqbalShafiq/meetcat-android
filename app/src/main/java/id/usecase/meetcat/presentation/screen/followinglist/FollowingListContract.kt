package id.usecase.meetcat.presentation.screen.followinglist

import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface FollowingListUiEvent {
    data object Refresh : FollowingListUiEvent
    data object LoadMore : FollowingListUiEvent
    data class NavigateToProfile(val userId: String) : FollowingListUiEvent
    data class ToggleFollow(val userId: String) : FollowingListUiEvent
    data object NavigateBack : FollowingListUiEvent
}

data class FollowingListUiState(
    val userId: String = "",
    val following: ImmutableList<User> = persistentListOf(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

sealed interface FollowingListUiEffect {
    data class NavigateToProfile(val userId: String) : FollowingListUiEffect
    data object NavigateBack : FollowingListUiEffect
    data class ShowError(val message: String) : FollowingListUiEffect
}
