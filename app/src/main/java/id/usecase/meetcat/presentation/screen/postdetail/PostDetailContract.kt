package id.usecase.meetcat.presentation.screen.postdetail

import androidx.compose.runtime.Immutable
import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.domain.model.Post
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PostDetailUiState(
    val post: Post? = null,
    val comments: ImmutableList<Comment> = persistentListOf(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

sealed class PostDetailUiEvent {
    data object Refresh : PostDetailUiEvent()
    data object NavigateBack : PostDetailUiEvent()
    data class NavigateToProfile(val userId: String) : PostDetailUiEvent()
    data object LovePost : PostDetailUiEvent()
    data class LoveComment(val commentId: String) : PostDetailUiEvent()
    data object CommentClick : PostDetailUiEvent()
    data object ReplyClick : PostDetailUiEvent()
    data object ShareClick : PostDetailUiEvent()
    data class SubmitComment(val commentText: String) : PostDetailUiEvent()
    data class SubmitReply(val replyText: String) : PostDetailUiEvent()
}

sealed class PostDetailUiEffect {
    data object NavigateBack : PostDetailUiEffect()
    data class NavigateToProfile(val userId: String) : PostDetailUiEffect()
    data class ShowError(val message: String) : PostDetailUiEffect()
    data class ShowCommentDialog(val postId: String) : PostDetailUiEffect()
    data class ShowReplyDialog(val postId: String) : PostDetailUiEffect()
    data class ShowShareDialog(val postId: String) : PostDetailUiEffect()
    data object CommentSubmitted : PostDetailUiEffect()
    data object ReplySubmitted : PostDetailUiEffect()
}
