package id.usecase.meetcat.presentation.screen.userprofile

import androidx.compose.runtime.Immutable
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class UserProfileUiState(
    val user: User? = null,
    val posts: ImmutableList<Post> = persistentListOf(),
    val replies: ImmutableList<FeedItem.ReplyItem> = persistentListOf(),
    val lovedItems: ImmutableList<FeedItem> = persistentListOf(),
    val selectedTab: ProfileTab = ProfileTab.POSTS,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

enum class ProfileTab {
    POSTS,
    REPLIES,
    LOVED
}

sealed class UserProfileUiEvent {
    data object Refresh : UserProfileUiEvent()
    data object LoadMore : UserProfileUiEvent()
    data class TabSelected(val tab: ProfileTab) : UserProfileUiEvent()
    data class NavigateToPost(val postId: String) : UserProfileUiEvent()
    data class LovePost(val postId: String) : UserProfileUiEvent()
    data class LoveReply(val replyId: String) : UserProfileUiEvent()
    data object ToggleFollow : UserProfileUiEvent()
    data object NavigateBack : UserProfileUiEvent()
}

sealed class UserProfileUiEffect {
    data class NavigateToPost(val postId: String) : UserProfileUiEffect()
    data class ShowError(val message: String) : UserProfileUiEffect()
    data object NavigateBack : UserProfileUiEffect()
}
