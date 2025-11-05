package id.usecase.meetcat.presentation.screen.profile

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileUiState(
    val user: User? = null,
    val selectedTab: ProfileTab = ProfileTab.POSTS,
    val posts: ImmutableList<Post> = persistentListOf(),
    val replies: ImmutableList<FeedItem.ReplyItem> = persistentListOf(),
    val lovedItems: ImmutableList<FeedItem> = persistentListOf(),
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
}

sealed interface ProfileUiEffect {
    data class NavigateToPost(val postId: String) : ProfileUiEffect
    data class ShowError(val message: String) : ProfileUiEffect
}
