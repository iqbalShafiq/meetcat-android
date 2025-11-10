package id.usecase.meetcat.presentation.screen.createpost

import android.net.Uri

sealed interface CreatePostUiEvent {
    data class CaptionChanged(val caption: String) : CreatePostUiEvent
    data object SelectMedia : CreatePostUiEvent
    data class MediaSelected(val uri: Uri) : CreatePostUiEvent
    data object RemoveMedia : CreatePostUiEvent
    data object SelectLocation : CreatePostUiEvent
    data class LocationSelected(val latitude: Double, val longitude: Double, val address: String) : CreatePostUiEvent
    data object RemoveLocation : CreatePostUiEvent
    data object CreatePost : CreatePostUiEvent
    data object NavigateBack : CreatePostUiEvent
}

data class CreatePostUiState(
    val caption: String = "",
    val mediaUri: Uri? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val captionError: String? = null
)

sealed interface CreatePostUiEffect {
    data object NavigateBack : CreatePostUiEffect
    data class NavigateToPostDetail(val postId: String) : CreatePostUiEffect
    data object ShowMediaPicker : CreatePostUiEffect
    data object ShowLocationPicker : CreatePostUiEffect
    data class ShowSuccess(val message: String) : CreatePostUiEffect
    data class ShowError(val message: String) : CreatePostUiEffect
}
