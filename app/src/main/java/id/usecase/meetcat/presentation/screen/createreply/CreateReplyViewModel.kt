package id.usecase.meetcat.presentation.screen.createreply

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

class CreateReplyViewModel(
    private val postId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateReplyUiState())
    val uiState: StateFlow<CreateReplyUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<CreateReplyUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadPost()
    }

    fun onEvent(event: CreateReplyUiEvent) {
        when (event) {
            is CreateReplyUiEvent.TextChanged -> {
                _uiState.update { it.copy(replyText = event.text, textError = null) }
            }

            is CreateReplyUiEvent.SelectMedia -> {
                viewModelScope.launch {
                    _uiEffect.send(CreateReplyUiEffect.ShowMediaPicker)
                }
            }

            is CreateReplyUiEvent.MediaSelected -> {
                _uiState.update { it.copy(mediaUri = event.uri) }
            }

            is CreateReplyUiEvent.RemoveMedia -> {
                _uiState.update { it.copy(mediaUri = null) }
            }

            is CreateReplyUiEvent.CreateReply -> {
                createReply()
            }

            is CreateReplyUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(CreateReplyUiEffect.NavigateBack)
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
            val mockPost = Post(
                id = postId,
                user = User(
                    id = "user1",
                    username = "catowner",
                    displayName = "Cat Owner",
                    profileImageUrl = null,
                    bio = null,
                    followersCount = 100,
                    followingCount = 50,
                    postsCount = 25,
                    isFollowing = false
                ),
                caption = "My cat is so cute!",
                imageUrl = null,
                lovesCount = 42,
                repliesCount = 10,
                sharesCount = 5,
                isLoved = false,
                createdAt = System.currentTimeMillis(),
                latitude = null,
                longitude = null
            )

            _uiState.update { it.copy(post = mockPost, isLoading = false) }
        }
    }

    private fun createReply() {
        val currentState = _uiState.value

        // Validate
        if (!validateReply(currentState.replyText, currentState.mediaUri)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPosting = true) }

            // Simulate posting
            delay(1500)

            _uiState.update { it.copy(isPosting = false) }
            _uiEffect.send(CreateReplyUiEffect.ShowSuccess("Reply posted!"))
            delay(500)
            _uiEffect.send(CreateReplyUiEffect.NavigateBack)
        }
    }

    private fun validateReply(text: String, mediaUri: Uri?): Boolean {
        if (text.isBlank() && mediaUri == null) {
            _uiState.update { it.copy(textError = "Please write something or add media") }
            return false
        }

        if (text.length > 280) {
            _uiState.update { it.copy(textError = "Reply must be 280 characters or less") }
            return false
        }

        return true
    }
}
