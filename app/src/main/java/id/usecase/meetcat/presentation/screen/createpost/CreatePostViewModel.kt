package id.usecase.meetcat.presentation.screen.createpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.repository.PostRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

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

            // Create location object if available
            val location = if (currentState.latitude != null && currentState.longitude != null) {
                Location(
                    latitude = currentState.latitude,
                    longitude = currentState.longitude,
                    address = currentState.locationAddress,
                    name = null
                )
            } else null

            // Prepare media URIs list
            val mediaUris = currentState.mediaUri?.let { listOf(it) }

            // Call repository to create post
            val result = postRepository.createPost(
                caption = currentState.caption,
                mediaUris = mediaUris,
                location = location
            )

            result.fold(
                onSuccess = { createdPost ->
                    _uiState.update { it.copy(isUploading = false, uploadProgress = 1f) }
                    _uiEffect.send(CreatePostUiEffect.ShowSuccess("Post created successfully!"))
                    delay(500)
                    _uiEffect.send(CreatePostUiEffect.NavigateBack)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 0f,
                            captionError = error.message ?: "Failed to create post"
                        )
                    }
                    _uiEffect.send(CreatePostUiEffect.ShowError(error.message ?: "Failed to create post"))
                }
            )
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
