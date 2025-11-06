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
    // Note: posts, replies, lovedItems moved to Paging3 Flows in ViewModel
    // (viewModel.posts, viewModel.replies, viewModel.lovedItems)
    val selectedTab: UserProfileTab = UserProfileTab.POSTS,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

enum class UserProfileTab {
    POSTS,
    REPLIES,
    LOVED
}

sealed class UserProfileUiEvent {
    data object Refresh : UserProfileUiEvent()
    data object LoadMore : UserProfileUiEvent()
    data class TabSelected(val tab: UserProfileTab) : UserProfileUiEvent()
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
