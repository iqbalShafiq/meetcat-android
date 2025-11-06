package id.usecase.meetcat.presentation.screen.replydetail

import androidx.compose.runtime.Immutable
import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.domain.model.Reply
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ReplyDetailUiState(
    val reply: Reply? = null,
    val comments: ImmutableList<Comment> = persistentListOf(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

sealed class ReplyDetailUiEvent {
    data object Refresh : ReplyDetailUiEvent()
    data object NavigateBack : ReplyDetailUiEvent()
    data class NavigateToProfile(val userId: String) : ReplyDetailUiEvent()
    data class NavigateToOriginalPost(val postId: String) : ReplyDetailUiEvent()
    data object LoveReply : ReplyDetailUiEvent()
    data class LoveComment(val commentId: String) : ReplyDetailUiEvent()
    data object CommentClick : ReplyDetailUiEvent()
    data object ShareClick : ReplyDetailUiEvent()
    data class SubmitComment(val commentText: String) : ReplyDetailUiEvent()
}

sealed class ReplyDetailUiEffect {
    data object NavigateBack : ReplyDetailUiEffect()
    data class NavigateToProfile(val userId: String) : ReplyDetailUiEffect()
    data class NavigateToOriginalPost(val postId: String) : ReplyDetailUiEffect()
    data class ShowError(val message: String) : ReplyDetailUiEffect()
    data class ShowCommentDialog(val replyId: String) : ReplyDetailUiEffect()
    data class ShowShareDialog(val replyId: String) : ReplyDetailUiEffect()
    data object CommentSubmitted : ReplyDetailUiEffect()
}
