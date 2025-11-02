package id.usecase.meetcat.presentation.screen.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.usecase.post.GetExploreFeedUseCase
import id.usecase.meetcat.domain.usecase.post.LovePostUseCase
import id.usecase.meetcat.domain.usecase.post.LoveReplyUseCase
import id.usecase.meetcat.domain.usecase.post.UnlovePostUseCase
import id.usecase.meetcat.domain.usecase.post.UnloveReplyUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val getExploreFeedUseCase: GetExploreFeedUseCase,
    private val lovePostUseCase: LovePostUseCase,
    private val unlovePostUseCase: UnlovePostUseCase,
    private val loveReplyUseCase: LoveReplyUseCase,
    private val unloveReplyUseCase: UnloveReplyUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ExploreUiEffect>()
    val uiEffect: Flow<ExploreUiEffect> = _uiEffect.receiveAsFlow()

    private var currentPage = 0

    init {
        loadInitialFeed()
    }

    fun onEvent(event: ExploreUiEvent) {
        when (event) {
            is ExploreUiEvent.Refresh -> refresh()
            is ExploreUiEvent.LoadMore -> loadMore()
            is ExploreUiEvent.LovePost -> toggleLovePost(event.postId)
            is ExploreUiEvent.LoveReply -> toggleLoveReply(event.replyId)
            is ExploreUiEvent.NavigateToPost -> {
                viewModelScope.launch {
                    _uiEffect.send(ExploreUiEffect.NavigateToPost(event.postId))
                }
            }
            is ExploreUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(ExploreUiEffect.NavigateToProfile(event.userId))
                }
            }
            is ExploreUiEvent.NavigateToComments -> {
                viewModelScope.launch {
                    _uiEffect.send(ExploreUiEffect.NavigateToComments(event.postId))
                }
            }
            is ExploreUiEvent.NavigateToReply -> {
                viewModelScope.launch {
                    _uiEffect.send(ExploreUiEffect.NavigateToReply(event.postId))
                }
            }
        }
    }

    private fun loadInitialFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = getExploreFeedUseCase(page = 0)

            result.fold(
                onSuccess = { items ->
                    currentPage = 0
                    _uiState.update {
                        it.copy(
                            feedItems = items.toImmutableList(),
                            isLoading = false,
                            hasMore = items.size >= PAGE_SIZE,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unknown error"
                        )
                    }
                }
            )
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val result = getExploreFeedUseCase(page = 0)

            result.fold(
                onSuccess = { items ->
                    currentPage = 0
                    _uiState.update {
                        it.copy(
                            feedItems = items.toImmutableList(),
                            isRefreshing = false,
                            hasMore = items.size >= PAGE_SIZE,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isRefreshing = false) }
                    _uiEffect.send(
                        ExploreUiEffect.ShowError(error.message ?: "Failed to refresh")
                    )
                }
            )
        }
    }

    private fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return

        viewModelScope.launch {
            val nextPage = currentPage + 1
            val result = getExploreFeedUseCase(page = nextPage)

            result.fold(
                onSuccess = { items ->
                    currentPage = nextPage
                    _uiState.update {
                        it.copy(
                            feedItems = (it.feedItems + items).toImmutableList(),
                            hasMore = items.size >= PAGE_SIZE
                        )
                    }
                },
                onFailure = { error ->
                    _uiEffect.send(
                        ExploreUiEffect.ShowError(error.message ?: "Failed to load more")
                    )
                }
            )
        }
    }

    private fun toggleLovePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    feedItems = state.feedItems.map { item ->
                        when (item) {
                            is FeedItem.PostItem -> {
                                if (item.post.id == postId) {
                                    FeedItem.PostItem(
                                        item.post.copy(
                                            isLoved = !item.post.isLoved,
                                            lovesCount = if (item.post.isLoved)
                                                item.post.lovesCount - 1
                                            else
                                                item.post.lovesCount + 1
                                        )
                                    )
                                } else item
                            }
                            is FeedItem.ReplyItem -> item
                        }
                    }.toImmutableList()
                )
            }

            val currentPost = _uiState.value.feedItems
                .filterIsInstance<FeedItem.PostItem>()
                .find { it.post.id == postId }?.post

            val result = if (currentPost?.isLoved == true) {
                unlovePostUseCase(postId)
            } else {
                lovePostUseCase(postId)
            }

            result.onFailure {
                _uiState.update { state ->
                    state.copy(
                        feedItems = state.feedItems.map { item ->
                            when (item) {
                                is FeedItem.PostItem -> {
                                    if (item.post.id == postId) {
                                        FeedItem.PostItem(
                                            item.post.copy(
                                                isLoved = !item.post.isLoved,
                                                lovesCount = if (item.post.isLoved)
                                                    item.post.lovesCount - 1
                                                else
                                                    item.post.lovesCount + 1
                                            )
                                        )
                                    } else item
                                }
                                is FeedItem.ReplyItem -> item
                            }
                        }.toImmutableList()
                    )
                }
                _uiEffect.send(ExploreUiEffect.ShowError("Failed to love post"))
            }
        }
    }

    private fun toggleLoveReply(replyId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    feedItems = state.feedItems.map { item ->
                        when (item) {
                            is FeedItem.ReplyItem -> {
                                if (item.reply.id == replyId) {
                                    FeedItem.ReplyItem(
                                        item.reply.copy(
                                            isLoved = !item.reply.isLoved,
                                            lovesCount = if (item.reply.isLoved)
                                                item.reply.lovesCount - 1
                                            else
                                                item.reply.lovesCount + 1
                                        )
                                    )
                                } else item
                            }
                            is FeedItem.PostItem -> item
                        }
                    }.toImmutableList()
                )
            }

            val currentReply = _uiState.value.feedItems
                .filterIsInstance<FeedItem.ReplyItem>()
                .find { it.reply.id == replyId }?.reply

            val result = if (currentReply?.isLoved == true) {
                unloveReplyUseCase(replyId)
            } else {
                loveReplyUseCase(replyId)
            }

            result.onFailure {
                _uiState.update { state ->
                    state.copy(
                        feedItems = state.feedItems.map { item ->
                            when (item) {
                                is FeedItem.ReplyItem -> {
                                    if (item.reply.id == replyId) {
                                        FeedItem.ReplyItem(
                                            item.reply.copy(
                                                isLoved = !item.reply.isLoved,
                                                lovesCount = if (item.reply.isLoved)
                                                    item.reply.lovesCount - 1
                                                else
                                                    item.reply.lovesCount + 1
                                            )
                                        )
                                    } else item
                                }
                                is FeedItem.PostItem -> item
                            }
                        }.toImmutableList()
                    )
                }
                _uiEffect.send(ExploreUiEffect.ShowError("Failed to love reply"))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
