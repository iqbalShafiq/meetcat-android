package id.usecase.meetcat.presentation.screen.editpost

import android.net.Uri
import id.usecase.meetcat.domain.model.Post

sealed interface EditPostUiEvent {
    data class CaptionChanged(val caption: String) : EditPostUiEvent
    data object SelectMedia : EditPostUiEvent
    data class MediaSelected(val uri: Uri) : EditPostUiEvent
    data object RemoveMedia : EditPostUiEvent
    data object SaveChanges : EditPostUiEvent
    data object DeletePost : EditPostUiEvent
    data object NavigateBack : EditPostUiEvent
}

data class EditPostUiState(
    val post: Post? = null,
    val caption: String = "",
    val mediaUri: Uri? = null,
    val originalImageUrl: String? = null,
    val hasMediaChanged: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val captionError: String? = null
)

sealed interface EditPostUiEffect {
    data object NavigateBack : EditPostUiEffect
    data class NavigateToPostDetail(val postId: String) : EditPostUiEffect
    data object ShowMediaPicker : EditPostUiEffect
    data object ShowDeleteConfirmation : EditPostUiEffect
    data class ShowSuccess(val message: String) : EditPostUiEffect
    data class ShowError(val message: String) : EditPostUiEffect
}
