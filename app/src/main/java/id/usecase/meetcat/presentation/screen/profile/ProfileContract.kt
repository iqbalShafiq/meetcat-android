package id.usecase.meetcat.presentation.screen.profile

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileUiState(
    val user: User? = null,
    val selectedTab: ProfileTab = ProfileTab.POSTS,
    // Note: posts, replies, lovedItems moved to Paging3 Flows in ViewModel
    // (viewModel.posts, viewModel.replies, viewModel.lovedItems)
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

enum class ProfileTab {
    POSTS,
    REPLIES,
    LOVED
}

sealed interface ProfileUiEvent {
    data object Refresh : ProfileUiEvent
    data object LoadMore : ProfileUiEvent
    data class TabSelected(val tab: ProfileTab) : ProfileUiEvent
    data class NavigateToPost(val postId: String) : ProfileUiEvent
    data class LovePost(val postId: String) : ProfileUiEvent
    data class LoveReply(val replyId: String) : ProfileUiEvent
    data object NavigateToSettings : ProfileUiEvent
}

sealed interface ProfileUiEffect {
    data class NavigateToPost(val postId: String) : ProfileUiEffect
    data class ShowError(val message: String) : ProfileUiEffect
    data object NavigateToSettings : ProfileUiEffect
}
