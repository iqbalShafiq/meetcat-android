package id.usecase.meetcat.presentation.screen.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.paging.ExplorePagingSource
import id.usecase.meetcat.domain.usecase.post.GetExploreFeedUseCase
import id.usecase.meetcat.domain.usecase.post.LovePostUseCase
import id.usecase.meetcat.domain.usecase.post.LoveReplyUseCase
import id.usecase.meetcat.domain.usecase.post.UnlovePostUseCase
import id.usecase.meetcat.domain.usecase.post.UnloveReplyUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _uiEffect = Channel<ExploreUiEffect>()
    val uiEffect: Flow<ExploreUiEffect> = _uiEffect.receiveAsFlow()

    // Track loved items separately for optimistic UI updates
    private val _lovedPosts = MutableStateFlow<Set<String>>(emptySet())
    private val _lovedReplies = MutableStateFlow<Set<String>>(emptySet())

    // Paging3 Flow for infinite scroll
    private val pagingFlow: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ExplorePagingSource(getExploreFeedUseCase) }
    ).flow.cachedIn(viewModelScope)

    // Combine paging data with love states for optimistic UI updates
    val feedItems: Flow<PagingData<FeedItem>> = combine(
        pagingFlow,
        _lovedPosts,
        _lovedReplies
    ) { pagingData, lovedPosts, lovedReplies ->
        pagingData.map { item ->
            when (item) {
                is FeedItem.PostItem -> {
                    val isLocallyLoved = lovedPosts.contains(item.post.id)
                    if (isLocallyLoved != item.post.isLoved) {
                        FeedItem.PostItem(
                            item.post.copy(
                                isLoved = isLocallyLoved,
                                lovesCount = if (isLocallyLoved)
                                    item.post.lovesCount + 1
                                else
                                    item.post.lovesCount - 1
                            )
                        )
                    } else item
                }
                is FeedItem.ReplyItem -> {
                    val isLocallyLoved = lovedReplies.contains(item.reply.id)
                    if (isLocallyLoved != item.reply.isLoved) {
                        FeedItem.ReplyItem(
                            item.reply.copy(
                                isLoved = isLocallyLoved,
                                lovesCount = if (isLocallyLoved)
                                    item.reply.lovesCount + 1
                                else
                                    item.reply.lovesCount - 1
                            )
                        )
                    } else item
                }
            }
        }
    }

    fun onEvent(event: ExploreUiEvent) {
        when (event) {
            is ExploreUiEvent.Refresh -> {
                // Refresh is handled by LazyPagingItems.refresh() in UI layer
            }
            is ExploreUiEvent.LoadMore -> {
                // LoadMore is handled automatically by Paging3
            }
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

    private fun toggleLovePost(postId: String) {
        viewModelScope.launch {
            // Optimistic update
            val isCurrentlyLoved = _lovedPosts.value.contains(postId)
            _lovedPosts.update { current ->
                if (isCurrentlyLoved) {
                    current - postId
                } else {
                    current + postId
                }
            }

            // Perform API call
            val result = if (isCurrentlyLoved) {
                unlovePostUseCase(postId)
            } else {
                lovePostUseCase(postId)
            }

            // Revert on failure
            result.onFailure {
                _lovedPosts.update { current ->
                    if (isCurrentlyLoved) {
                        current + postId
                    } else {
                        current - postId
                    }
                }
                _uiEffect.send(ExploreUiEffect.ShowError("Failed to love post"))
            }
        }
    }

    private fun toggleLoveReply(replyId: String) {
        viewModelScope.launch {
            // Optimistic update
            val isCurrentlyLoved = _lovedReplies.value.contains(replyId)
            _lovedReplies.update { current ->
                if (isCurrentlyLoved) {
                    current - replyId
                } else {
                    current + replyId
                }
            }

            // Perform API call
            val result = if (isCurrentlyLoved) {
                unloveReplyUseCase(replyId)
            } else {
                loveReplyUseCase(replyId)
            }

            // Revert on failure
            result.onFailure {
                _lovedReplies.update { current ->
                    if (isCurrentlyLoved) {
                        current + replyId
                    } else {
                        current - replyId
                    }
                }
                _uiEffect.send(ExploreUiEffect.ShowError("Failed to love reply"))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10 // Load more when 10 items from bottom
    }
}
