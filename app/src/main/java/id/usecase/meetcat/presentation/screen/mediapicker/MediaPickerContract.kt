package id.usecase.meetcat.presentation.screen.mediapicker

import android.net.Uri

enum class MediaPickerMode {
    GALLERY,
    CAMERA
}

sealed interface MediaPickerUiEvent {
    data class ModeSelected(val mode: MediaPickerMode) : MediaPickerUiEvent
    data object TakePhoto : MediaPickerUiEvent
    data class MediaSelected(val uri: Uri) : MediaPickerUiEvent
    data object NavigateBack : MediaPickerUiEvent
}

data class MediaPickerUiState(
    val selectedMode: MediaPickerMode = MediaPickerMode.GALLERY,
    val selectedMediaUri: Uri? = null,
    val isLoading: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    val storagePermissionGranted: Boolean = false
)

sealed interface MediaPickerUiEffect {
    data class MediaSelected(val uri: Uri) : MediaPickerUiEffect
    data object NavigateBack : MediaPickerUiEffect
    data object RequestCameraPermission : MediaPickerUiEffect
    data object RequestStoragePermission : MediaPickerUiEffect
    data object LaunchCamera : MediaPickerUiEffect
    data object LaunchGallery : MediaPickerUiEffect
    data class ShowError(val message: String) : MediaPickerUiEffect
}
