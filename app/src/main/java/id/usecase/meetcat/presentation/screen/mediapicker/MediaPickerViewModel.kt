package id.usecase.meetcat.presentation.screen.mediapicker

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MediaPickerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MediaPickerUiState())
    val uiState: StateFlow<MediaPickerUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MediaPickerUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: MediaPickerUiEvent) {
        when (event) {
            is MediaPickerUiEvent.ModeSelected -> {
                _uiState.update { it.copy(selectedMode = event.mode) }
                when (event.mode) {
                    MediaPickerMode.GALLERY -> {
                        checkStoragePermissionAndLaunchGallery()
                    }
                    MediaPickerMode.CAMERA -> {
                        checkCameraPermissionAndLaunchCamera()
                    }
                }
            }

            is MediaPickerUiEvent.TakePhoto -> {
                checkCameraPermissionAndLaunchCamera()
            }

            is MediaPickerUiEvent.MediaSelected -> {
                selectMedia(event.uri)
            }

            is MediaPickerUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(MediaPickerUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun checkCameraPermissionAndLaunchCamera() {
        viewModelScope.launch {
            if (_uiState.value.cameraPermissionGranted) {
                _uiEffect.send(MediaPickerUiEffect.LaunchCamera)
            } else {
                _uiEffect.send(MediaPickerUiEffect.RequestCameraPermission)
            }
        }
    }

    private fun checkStoragePermissionAndLaunchGallery() {
        viewModelScope.launch {
            if (_uiState.value.storagePermissionGranted) {
                _uiEffect.send(MediaPickerUiEffect.LaunchGallery)
            } else {
                _uiEffect.send(MediaPickerUiEffect.RequestStoragePermission)
            }
        }
    }

    private fun selectMedia(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedMediaUri = uri) }
            _uiEffect.send(MediaPickerUiEffect.MediaSelected(uri))
            _uiEffect.send(MediaPickerUiEffect.NavigateBack)
        }
    }

    fun onPermissionResult(permissionGranted: Boolean, isCamera: Boolean) {
        if (isCamera) {
            _uiState.update { it.copy(cameraPermissionGranted = permissionGranted) }
            if (permissionGranted) {
                viewModelScope.launch {
                    _uiEffect.send(MediaPickerUiEffect.LaunchCamera)
                }
            } else {
                viewModelScope.launch {
                    _uiEffect.send(MediaPickerUiEffect.ShowError("Camera permission denied"))
                }
            }
        } else {
            _uiState.update { it.copy(storagePermissionGranted = permissionGranted) }
            if (permissionGranted) {
                viewModelScope.launch {
                    _uiEffect.send(MediaPickerUiEffect.LaunchGallery)
                }
            } else {
                viewModelScope.launch {
                    _uiEffect.send(MediaPickerUiEffect.ShowError("Storage permission denied"))
                }
            }
        }
    }
}
