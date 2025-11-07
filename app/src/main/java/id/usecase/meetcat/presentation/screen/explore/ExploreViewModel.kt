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
import id.usecase.meetcat.domain.usecase.auth.GetCurrentUserUseCase
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
    private val unloveReplyUseCase: UnloveReplyUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiEffect = Channel<ExploreUiEffect>()
    val uiEffect: Flow<ExploreUiEffect> = _uiEffect.receiveAsFlow()

    // Current user ID
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // Preserve scroll position across navigation
    var scrollIndex: Int = 0
    var scrollOffset: Int = 0

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            _currentUserId.value = currentUser?.id
        }
    }

    // Track love toggles for optimistic UI updates
    // Set contains IDs that have been toggled from their original state
    private val _toggledPosts = MutableStateFlow<Set<String>>(emptySet())
    private val _toggledReplies = MutableStateFlow<Set<String>>(emptySet())

    // Paging3 Flow for infinite scroll
    private val pagingFlow: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            prefetchDistance = PREFETCH_DISTANCE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ExplorePagingSource(getExploreFeedUseCase) }
    ).flow.cachedIn(viewModelScope)

    // Combine paging data with love toggles for optimistic UI updates
    val feedItems: Flow<PagingData<FeedItem>> = combine(
        pagingFlow,
        _toggledPosts,
        _toggledReplies
    ) { pagingData, toggledPosts, toggledReplies ->
        pagingData.map { item ->
            when (item) {
                is FeedItem.PostItem -> {
                    val isToggled = toggledPosts.contains(item.post.id)
                    if (isToggled) {
                        // Toggle the love state and adjust count
                        FeedItem.PostItem(
                            item.post.copy(
                                isLoved = !item.post.isLoved, // Toggle from original
                                lovesCount = if (item.post.isLoved)
                                    item.post.lovesCount - 1  // Was loved, now unloved
                                else
                                    item.post.lovesCount + 1  // Was unloved, now loved
                            )
                        )
                    } else item
                }
                is FeedItem.ReplyItem -> {
                    val isToggled = toggledReplies.contains(item.reply.id)
                    if (isToggled) {
                        // Toggle the love state and adjust count
                        FeedItem.ReplyItem(
                            item.reply.copy(
                                isLoved = !item.reply.isLoved, // Toggle from original
                                lovesCount = if (item.reply.isLoved)
                                    item.reply.lovesCount - 1  // Was loved, now unloved
                                else
                                    item.reply.lovesCount + 1  // Was unloved, now loved
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
            is ExploreUiEvent.NavigateToEditPost -> {
                viewModelScope.launch {
                    _uiEffect.send(ExploreUiEffect.NavigateToEditPost(event.postId))
                }
            }
        }
    }

    private fun toggleLovePost(postId: String) {
        viewModelScope.launch {
            // Optimistic update: toggle the item state
            val isCurrentlyToggled = _toggledPosts.value.contains(postId)
            _toggledPosts.update { current ->
                if (isCurrentlyToggled) {
                    // Already toggled, un-toggle it (back to original state)
                    current - postId
                } else {
                    // Not toggled, toggle it
                    current + postId
                }
            }

            // Determine action based on current display state
            // Note: This assumes we're toggling from the currently displayed state
            // If toggled=false (showing original), we're now adding toggle (loving)
            // If toggled=true (showing toggled), we're now removing toggle (unloving)
            val result = if (!isCurrentlyToggled) {
                lovePostUseCase(postId)
            } else {
                unlovePostUseCase(postId)
            }

            // Revert on failure
            result.onFailure {
                _toggledPosts.update { current ->
                    if (isCurrentlyToggled) {
                        current + postId
                    } else {
                        current - postId
                    }
                }
                _uiEffect.send(ExploreUiEffect.ShowError("Failed to update love"))
            }
        }
    }

    private fun toggleLoveReply(replyId: String) {
        viewModelScope.launch {
            // Optimistic update: toggle the item state
            val isCurrentlyToggled = _toggledReplies.value.contains(replyId)
            _toggledReplies.update { current ->
                if (isCurrentlyToggled) {
                    // Already toggled, un-toggle it (back to original state)
                    current - replyId
                } else {
                    // Not toggled, toggle it
                    current + replyId
                }
            }

            // Determine action based on toggle state
            // If newly toggled: love, if un-toggled: unlove
            val result = if (!isCurrentlyToggled) {
                loveReplyUseCase(replyId)
            } else {
                unloveReplyUseCase(replyId)
            }

            // Revert on failure
            result.onFailure {
                _toggledReplies.update { current ->
                    if (isCurrentlyToggled) {
                        current + replyId
                    } else {
                        current - replyId
                    }
                }
                _uiEffect.send(ExploreUiEffect.ShowError("Failed to update love"))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10 // Load more when 10 items from bottom
    }
}
