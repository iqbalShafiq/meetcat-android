package id.usecase.meetcat.presentation.screen.createpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreatePostViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CreatePostUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: CreatePostUiEvent) {
        when (event) {
            is CreatePostUiEvent.CaptionChanged -> {
                _uiState.update { it.copy(caption = event.caption, captionError = null) }
            }

            is CreatePostUiEvent.SelectMedia -> {
                viewModelScope.launch {
                    _uiEffect.send(CreatePostUiEffect.ShowMediaPicker)
                }
            }

            is CreatePostUiEvent.MediaSelected -> {
                _uiState.update { it.copy(mediaUri = event.uri) }
            }

            is CreatePostUiEvent.RemoveMedia -> {
                _uiState.update { it.copy(mediaUri = null) }
            }

            is CreatePostUiEvent.SelectLocation -> {
                viewModelScope.launch {
                    _uiEffect.send(CreatePostUiEffect.ShowLocationPicker)
                }
            }

            is CreatePostUiEvent.LocationSelected -> {
                _uiState.update {
                    it.copy(
                        latitude = event.latitude,
                        longitude = event.longitude,
                        locationAddress = event.address
                    )
                }
            }

            is CreatePostUiEvent.RemoveLocation -> {
                _uiState.update {
                    it.copy(
                        latitude = null,
                        longitude = null,
                        locationAddress = null
                    )
                }
            }

            is CreatePostUiEvent.CreatePost -> {
                createPost()
            }

            is CreatePostUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(CreatePostUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun createPost() {
        val currentState = _uiState.value

        // Validate
        if (!validatePost(currentState.caption, currentState.mediaUri)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0f) }

            // Simulate upload with progress
            for (progress in 0..100 step 10) {
                delay(200)
                _uiState.update { it.copy(uploadProgress = progress / 100f) }
            }

            // Simulate successful upload
            delay(500)

            _uiState.update { it.copy(isUploading = false, uploadProgress = 0f) }
            _uiEffect.send(CreatePostUiEffect.ShowSuccess("Post created successfully!"))
            delay(500)
            _uiEffect.send(CreatePostUiEffect.NavigateBack)
        }
    }

    private fun validatePost(caption: String, mediaUri: Uri?): Boolean {
        if (caption.isBlank() && mediaUri == null) {
            _uiState.update { it.copy(captionError = "Please add a caption or photo") }
            return false
        }

        if (caption.length > 500) {
            _uiState.update { it.copy(captionError = "Caption must be 500 characters or less") }
            return false
        }

        return true
    }
}
