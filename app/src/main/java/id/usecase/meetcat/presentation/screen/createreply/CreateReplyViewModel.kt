package id.usecase.meetcat.presentation.screen.createreply

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.presentation.common.FeedStateManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateReplyViewModel(
    private val postRepository: PostRepository,
    private val postId: String,
    private val feedStateManager: FeedStateManager
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

            val result = postRepository.getPostById(postId)

            result.fold(
                onSuccess = { post ->
                    _uiState.update { it.copy(post = post, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(CreateReplyUiEffect.ShowError(error.message ?: "Failed to load post"))
                }
            )
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

            // Prepare media URIs list
            val mediaUris = currentState.mediaUri?.let { listOf(it) }

            // Call repository to create reply
            val result = postRepository.createReply(
                originalPostId = postId,
                text = currentState.replyText,
                mediaUris = mediaUris,
                location = null
            )

            result.fold(
                onSuccess = { createdReply ->
                    _uiState.update { it.copy(isPosting = false) }
                    _uiEffect.send(CreateReplyUiEffect.ShowSuccess("Reply posted!"))
                    // Notify feed state manager that a new reply was created
                    feedStateManager.notifyReplyCreated()
                    delay(500)
                    _uiEffect.send(CreateReplyUiEffect.NavigateToReplyDetail(createdReply.id))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isPosting = false,
                            textError = error.message ?: "Failed to post reply"
                        )
                    }
                    _uiEffect.send(CreateReplyUiEffect.ShowError(error.message ?: "Failed to post reply"))
                }
            )
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
