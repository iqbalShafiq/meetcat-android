package id.usecase.meetcat.presentation.screen.explore

import androidx.compose.runtime.Immutable
import id.usecase.meetcat.domain.model.FeedItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ExploreUiState(
    val feedItems: ImmutableList<FeedItem> = persistentListOf(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true
)

sealed class ExploreUiEvent {
    data object Refresh : ExploreUiEvent()
    data object LoadMore : ExploreUiEvent()
    data class LovePost(val postId: String) : ExploreUiEvent()
    data class LoveReply(val replyId: String) : ExploreUiEvent()
    data class NavigateToPost(val postId: String) : ExploreUiEvent()
    data class NavigateToProfile(val userId: String) : ExploreUiEvent()
    data class NavigateToComments(val postId: String) : ExploreUiEvent()
    data class NavigateToReply(val postId: String) : ExploreUiEvent()
    data class NavigateToEditPost(val postId: String) : ExploreUiEvent()
    data object NewPostsChipClick : ExploreUiEvent()
    data object DismissNewPostsChip : ExploreUiEvent()
}

sealed class ExploreUiEffect {
    data class NavigateToPost(val postId: String) : ExploreUiEffect()
    data class NavigateToProfile(val userId: String) : ExploreUiEffect()
    data class NavigateToComments(val postId: String) : ExploreUiEffect()
    data class NavigateToReply(val postId: String) : ExploreUiEffect()
    data class NavigateToEditPost(val postId: String) : ExploreUiEffect()
    data class ShowError(val message: String) : ExploreUiEffect()
}
