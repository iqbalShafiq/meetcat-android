package id.usecase.meetcat.presentation.screen.postdetail

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

class PostDetailViewModel(
    private val postRepository: PostRepository,
    private val postId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PostDetailUiEffect>()
    val uiEffect: Flow<PostDetailUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadPostDetail()
    }

    fun onEvent(event: PostDetailUiEvent) {
        when (event) {
            is PostDetailUiEvent.Refresh -> refresh()
            is PostDetailUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(PostDetailUiEffect.NavigateBack)
                }
            }
            is PostDetailUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(PostDetailUiEffect.NavigateToProfile(event.userId))
                }
            }
            is PostDetailUiEvent.LovePost -> toggleLovePost()
            is PostDetailUiEvent.LoveComment -> toggleLoveComment(event.commentId)
            is PostDetailUiEvent.CommentClick -> {
                viewModelScope.launch {
                    _uiEffect.send(PostDetailUiEffect.ShowCommentDialog(postId))
                }
            }
            is PostDetailUiEvent.ReplyClick -> {
                viewModelScope.launch {
                    _uiEffect.send(PostDetailUiEffect.ShowReplyDialog(postId))
                }
            }
            is PostDetailUiEvent.ShareClick -> {
                viewModelScope.launch {
                    _uiEffect.send(PostDetailUiEffect.ShowShareDialog(postId))
                }
            }
        }
    }

    private fun loadPostDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val postResult = postRepository.getPostById(postId)
            val commentsResult = postRepository.getPostComments(postId)

            postResult.fold(
                onSuccess = { post ->
                    commentsResult.fold(
                        onSuccess = { comments ->
                            _uiState.update {
                                it.copy(
                                    post = post,
                                    comments = comments.toImmutableList(),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    post = post,
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
                            error = error.message ?: "Failed to load post"
                        )
                    }
                }
            )
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val postResult = postRepository.getPostById(postId)
            val commentsResult = postRepository.getPostComments(postId)

            postResult.fold(
                onSuccess = { post ->
                    commentsResult.fold(
                        onSuccess = { comments ->
                            _uiState.update {
                                it.copy(
                                    post = post,
                                    comments = comments.toImmutableList(),
                                    isRefreshing = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isRefreshing = false) }
                            _uiEffect.send(
                                PostDetailUiEffect.ShowError(error.message ?: "Failed to refresh comments")
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isRefreshing = false) }
                    _uiEffect.send(
                        PostDetailUiEffect.ShowError(error.message ?: "Failed to refresh")
                    )
                }
            )
        }
    }

    private fun toggleLovePost() {
        val currentPost = _uiState.value.post ?: return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    post = currentPost.copy(
                        isLoved = !currentPost.isLoved,
                        lovesCount = if (currentPost.isLoved)
                            currentPost.lovesCount - 1
                        else
                            currentPost.lovesCount + 1
                    )
                )
            }

            val result = if (currentPost.isLoved) {
                postRepository.unlovePost(postId)
            } else {
                postRepository.lovePost(postId)
            }

            result.onFailure {
                _uiState.update { state ->
                    state.copy(
                        post = currentPost.copy(
                            isLoved = !currentPost.isLoved,
                            lovesCount = if (currentPost.isLoved)
                                currentPost.lovesCount - 1
                            else
                                currentPost.lovesCount + 1
                        )
                    )
                }
                _uiEffect.send(PostDetailUiEffect.ShowError("Failed to love post"))
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
                _uiEffect.send(PostDetailUiEffect.ShowError("Failed to love comment"))
            }
        }
    }
}
