package id.usecase.meetcat.presentation.screen.editpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditPostViewModel(
    private val postId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPostUiState())
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EditPostUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadPost()
    }

    fun onEvent(event: EditPostUiEvent) {
        when (event) {
            is EditPostUiEvent.CaptionChanged -> {
                _uiState.update { it.copy(caption = event.caption, captionError = null) }
            }

            is EditPostUiEvent.SelectMedia -> {
                viewModelScope.launch {
                    _uiEffect.send(EditPostUiEffect.ShowMediaPicker)
                }
            }

            is EditPostUiEvent.MediaSelected -> {
                _uiState.update { it.copy(mediaUri = event.uri, hasMediaChanged = true) }
            }

            is EditPostUiEvent.RemoveMedia -> {
                _uiState.update { it.copy(mediaUri = null, hasMediaChanged = true) }
            }

            is EditPostUiEvent.SaveChanges -> {
                saveChanges()
            }

            is EditPostUiEvent.DeletePost -> {
                viewModelScope.launch {
                    _uiEffect.send(EditPostUiEffect.ShowDeleteConfirmation)
                }
            }

            is EditPostUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(EditPostUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadPost() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate loading
            delay(500)

            // Mock post data
            val mockImageUrl = "https://example.com/image.jpg"
            val mockPost = Post(
                id = postId,
                userId = "currentUser",
                user = User(
                    id = "currentUser",
                    username = "myusername",
                    displayName = "My Name",
                    profileImageUrl = null,
                    bio = null,
                    followersCount = 100,
                    followingCount = 50,
                    postsCount = 25,
                    isFollowing = false
                ),
                caption = "My cat is so cute!",
                mediaItems = listOf(
                    MediaItem.Image(
                        url = mockImageUrl,
                        thumbnailUrl = mockImageUrl,
                        width = 800,
                        height = 600
                    )
                ),
                location = null,
                lovesCount = 42,
                commentsCount = 0,
                repliesCount = 10,
                isLoved = false,
                createdAt = System.currentTimeMillis()
            )

            _uiState.update {
                it.copy(
                    post = mockPost,
                    caption = mockPost.caption,
                    originalImageUrl = mockPost.mediaItems.firstOrNull()?.url,
                    isLoading = false
                )
            }
        }
    }

    private fun saveChanges() {
        val currentState = _uiState.value

        // Validate
        if (!validatePost(currentState.caption)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Simulate save
            delay(1500)

            _uiState.update { it.copy(isSaving = false) }
            _uiEffect.send(EditPostUiEffect.ShowSuccess("Post updated successfully!"))
            delay(500)
            _uiEffect.send(EditPostUiEffect.NavigateBack)
        }
    }

    private fun validatePost(caption: String): Boolean {
        if (caption.isBlank()) {
            _uiState.update { it.copy(captionError = "Caption cannot be empty") }
            return false
        }

        if (caption.length > 500) {
            _uiState.update { it.copy(captionError = "Caption must be 500 characters or less") }
            return false
        }

        return true
    }

    fun deletePost() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Simulate delete
            delay(1000)

            _uiState.update { it.copy(isSaving = false) }
            _uiEffect.send(EditPostUiEffect.ShowSuccess("Post deleted"))
            delay(500)
            _uiEffect.send(EditPostUiEffect.NavigateBack)
        }
    }
}
