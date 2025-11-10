package id.usecase.meetcat.presentation.screen.createreply

import android.net.Uri
import id.usecase.meetcat.domain.model.Post

sealed interface CreateReplyUiEvent {
    data class TextChanged(val text: String) : CreateReplyUiEvent
    data object SelectMedia : CreateReplyUiEvent
    data class MediaSelected(val uri: Uri) : CreateReplyUiEvent
    data object RemoveMedia : CreateReplyUiEvent
    data object CreateReply : CreateReplyUiEvent
    data object NavigateBack : CreateReplyUiEvent
}

data class CreateReplyUiState(
    val post: Post? = null,
    val replyText: String = "",
    val mediaUri: Uri? = null,
    val isLoading: Boolean = false,
    val isPosting: Boolean = false,
    val textError: String? = null
)

sealed interface CreateReplyUiEffect {
    data object NavigateBack : CreateReplyUiEffect
    data class NavigateToReplyDetail(val replyId: String) : CreateReplyUiEffect
    data object ShowMediaPicker : CreateReplyUiEffect
    data class ShowSuccess(val message: String) : CreateReplyUiEffect
    data class ShowError(val message: String) : CreateReplyUiEffect
}
