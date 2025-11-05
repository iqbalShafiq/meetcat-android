package id.usecase.meetcat.presentation.screen.replydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.repository.PostRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReplyDetailViewModel(
    private val postRepository: PostRepository,
    private val replyId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReplyDetailUiState())
    val uiState: StateFlow<ReplyDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ReplyDetailUiEffect>()
    val uiEffect: Flow<ReplyDetailUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadReplyDetail()
    }

    fun onEvent(event: ReplyDetailUiEvent) {
        when (event) {
            is ReplyDetailUiEvent.Refresh -> refresh()
            is ReplyDetailUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(ReplyDetailUiEffect.NavigateBack)
                }
            }
            is ReplyDetailUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(ReplyDetailUiEffect.NavigateToProfile(event.userId))
                }
            }
            is ReplyDetailUiEvent.NavigateToOriginalPost -> {
                viewModelScope.launch {
                    _uiEffect.send(ReplyDetailUiEffect.NavigateToOriginalPost(event.postId))
                }
            }
            is ReplyDetailUiEvent.LoveReply -> toggleLoveReply()
            is ReplyDetailUiEvent.LoveComment -> toggleLoveComment(event.commentId)
            is ReplyDetailUiEvent.CommentClick -> {
                viewModelScope.launch {
                    _uiEffect.send(ReplyDetailUiEffect.ShowCommentDialog(replyId))
                }
            }
            is ReplyDetailUiEvent.ShareClick -> {
                viewModelScope.launch {
                    _uiEffect.send(ReplyDetailUiEffect.ShowShareDialog(replyId))
                }
            }
        }
    }

    private fun loadReplyDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val replyResult = postRepository.getReplyById(replyId)
            val commentsResult = postRepository.getReplyComments(replyId)

            replyResult.fold(
                onSuccess = { reply ->
                    commentsResult.fold(
                        onSuccess = { comments ->
                            _uiState.update {
                                it.copy(
                                    reply = reply,
                                    comments = comments.toImmutableList(),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    reply = reply,
                                    isLoading = false,
                                    error = error.message ?: "Failed to load comments"
                                )
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load reply"
                        )
                    }
                }
            )
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val replyResult = postRepository.getReplyById(replyId)
            val commentsResult = postRepository.getReplyComments(replyId)

            replyResult.fold(
                onSuccess = { reply ->
                    commentsResult.fold(
                        onSuccess = { comments ->
                            _uiState.update {
                                it.copy(
                                    reply = reply,
                                    comments = comments.toImmutableList(),
                                    isRefreshing = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isRefreshing = false) }
                            _uiEffect.send(
                                ReplyDetailUiEffect.ShowError(error.message ?: "Failed to refresh comments")
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isRefreshing = false) }
                    _uiEffect.send(
                        ReplyDetailUiEffect.ShowError(error.message ?: "Failed to refresh")
                    )
                }
            )
        }
    }

    private fun toggleLoveReply() {
        val currentReply = _uiState.value.reply ?: return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    reply = currentReply.copy(
                        isLoved = !currentReply.isLoved,
                        lovesCount = if (currentReply.isLoved)
                            currentReply.lovesCount - 1
                        else
                            currentReply.lovesCount + 1
                    )
                )
            }

            val result = if (currentReply.isLoved) {
                postRepository.unloveReply(replyId)
            } else {
                postRepository.loveReply(replyId)
            }

            result.onFailure {
                _uiState.update { state ->
                    state.copy(
                        reply = currentReply.copy(
                            isLoved = !currentReply.isLoved,
                            lovesCount = if (currentReply.isLoved)
                                currentReply.lovesCount - 1
                            else
                                currentReply.lovesCount + 1
                        )
                    )
                }
                _uiEffect.send(ReplyDetailUiEffect.ShowError("Failed to love reply"))
            }
        }
    }

    private fun toggleLoveComment(commentId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    comments = state.comments.map { comment ->
                        if (comment.id == commentId) {
                            comment.copy(
                                isLoved = !comment.isLoved,
                                lovesCount = if (comment.isLoved)
                                    comment.lovesCount - 1
                                else
                                    comment.lovesCount + 1
                            )
                        } else comment
                    }.toImmutableList()
                )
            }

            val currentComment = _uiState.value.comments.find { it.id == commentId }
            val result = if (currentComment?.isLoved == true) {
                postRepository.unloveComment(commentId)
            } else {
                postRepository.loveComment(commentId)
            }

            result.onFailure {
                _uiState.update { state ->
                    state.copy(
                        comments = state.comments.map { comment ->
                            if (comment.id == commentId) {
                                comment.copy(
                                    isLoved = !comment.isLoved,
                                    lovesCount = if (comment.isLoved)
                                        comment.lovesCount - 1
                                    else
                                        comment.lovesCount + 1
                                )
                            } else comment
                        }.toImmutableList()
                    )
                }
                _uiEffect.send(ReplyDetailUiEffect.ShowError("Failed to love comment"))
            }
        }
    }
}
