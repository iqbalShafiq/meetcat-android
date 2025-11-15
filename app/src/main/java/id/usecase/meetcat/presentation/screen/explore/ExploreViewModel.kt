package id.usecase.meetcat.presentation.screen.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import id.usecase.meetcat.data.network.NetworkMonitor
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.paging.ExplorePagingSource
import id.usecase.meetcat.domain.usecase.auth.GetCurrentUserUseCase
import id.usecase.meetcat.domain.usecase.post.CheckNewPostsUseCase
import id.usecase.meetcat.domain.usecase.post.GetExploreFeedUseCase
import id.usecase.meetcat.domain.usecase.post.LovePostUseCase
import id.usecase.meetcat.domain.usecase.post.LoveReplyUseCase
import id.usecase.meetcat.domain.usecase.post.UnlovePostUseCase
import id.usecase.meetcat.domain.usecase.post.UnloveReplyUseCase
import id.usecase.meetcat.presentation.common.FeedStateManager
import id.usecase.meetcat.presentation.common.SnackbarController
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val getExploreFeedUseCase: GetExploreFeedUseCase,
    private val lovePostUseCase: LovePostUseCase,
    private val unlovePostUseCase: UnlovePostUseCase,
    private val loveReplyUseCase: LoveReplyUseCase,
    private val unloveReplyUseCase: UnloveReplyUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val checkNewPostsUseCase: CheckNewPostsUseCase,
    private val feedStateManager: FeedStateManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiEffect = Channel<ExploreUiEffect>()
    val uiEffect: Flow<ExploreUiEffect> = _uiEffect.receiveAsFlow()

    // Current user ID
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // New posts chip visibility
    private val _showNewPostsChip = MutableStateFlow(false)
    val showNewPostsChip: StateFlow<Boolean> = _showNewPostsChip.asStateFlow()

    // Track the timestamp of the latest visible post for polling comparison
    private var currentLatestTimestamp: Long = 0L

    // Preserve scroll position across navigation
    var scrollIndex: Int = 0
    var scrollOffset: Int = 0

    // Polling job for checking new posts
    private var pollingJob: Job? = null

    // Track ongoing jobs to prevent double-tap
    private val lovePostJobs = mutableMapOf<String, Job>()
    private val loveReplyJobs = mutableMapOf<String, Job>()

    // Track failure counts for retry limits
    private val postFailureCounts = mutableMapOf<String, Int>()
    private val replyFailureCounts = mutableMapOf<String, Int>()

    init {
        loadCurrentUser()
        observeFeedStateManager()
        startPolling()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            _currentUserId.value = currentUser?.id
        }
    }

    private fun observeFeedStateManager() {
        viewModelScope.launch {
            feedStateManager.shouldRefreshFeed.collect { shouldRefresh ->
                if (shouldRefresh) {
                    _showNewPostsChip.value = true
                }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(POLLING_INTERVAL_MS)

                // Only poll if online and we have a timestamp to compare
                if (networkMonitor.isOnline() && currentLatestTimestamp > 0L) {
                    checkNewPostsUseCase(currentLatestTimestamp)
                        .onSuccess { hasNewPosts ->
                            if (hasNewPosts) {
                                feedStateManager.notifyNewPostsDetected()
                            }
                        }
                        .onFailure {
                            // Silently fail polling, don't interrupt user experience
                        }
                }
            }
        }
    }

    fun updateLatestTimestamp(timestamp: Long) {
        currentLatestTimestamp = timestamp
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
            is ExploreUiEvent.NewPostsChipClick -> {
                // Hide chip and reset feed state
                _showNewPostsChip.value = false
                feedStateManager.resetRefreshState()
                // Scroll to top and refresh handled in UI layer
            }
            is ExploreUiEvent.DismissNewPostsChip -> {
                _showNewPostsChip.value = false
                feedStateManager.resetRefreshState()
            }
        }
    }

    private fun toggleLovePost(postId: String) {
        // Cancel previous job if still running (prevent double-tap)
        lovePostJobs[postId]?.cancel()

        lovePostJobs[postId] = viewModelScope.launch {
            // Check offline
            if (!networkMonitor.isOnline()) {
                SnackbarController.showError("No internet connection")
                return@launch
            }

            // Optimistic update: toggle the item state
            val isCurrentlyToggled = _toggledPosts.value.contains(postId)
            _toggledPosts.update { current ->
                if (isCurrentlyToggled) {
                    current - postId
                } else {
                    current + postId
                }
            }

            // Determine action based on toggle state
            val result = if (!isCurrentlyToggled) {
                lovePostUseCase(postId)
            } else {
                unlovePostUseCase(postId)
            }

            // Handle result
            result.onSuccess {
                // Reset failure count on success
                postFailureCounts.remove(postId)
            }.onFailure {
                // Revert optimistic update
                _toggledPosts.update { current ->
                    if (isCurrentlyToggled) {
                        current + postId
                    } else {
                        current - postId
                    }
                }

                // Track failures
                val failureCount = postFailureCounts.getOrDefault(postId, 0) + 1
                postFailureCounts[postId] = failureCount

                // Show appropriate error message
                val message = when {
                    failureCount >= MAX_RETRIES -> "Having trouble connecting. Please check your internet."
                    else -> "Failed to ${if (isCurrentlyToggled) "unlove" else "love"} post"
                }

                SnackbarController.showError(
                    message = message,
                    actionLabel = "Retry",
                    onRetry = if (failureCount < MAX_RETRIES) {
                        { toggleLovePost(postId) }
                    } else null
                )
            }

            lovePostJobs.remove(postId)
        }
    }

    private fun toggleLoveReply(replyId: String) {
        // Cancel previous job if still running (prevent double-tap)
        loveReplyJobs[replyId]?.cancel()

        loveReplyJobs[replyId] = viewModelScope.launch {
            // Check offline
            if (!networkMonitor.isOnline()) {
                SnackbarController.showError("No internet connection")
                return@launch
            }

            // Optimistic update: toggle the item state
            val isCurrentlyToggled = _toggledReplies.value.contains(replyId)
            _toggledReplies.update { current ->
                if (isCurrentlyToggled) {
                    current - replyId
                } else {
                    current + replyId
                }
            }

            // Determine action based on toggle state
            val result = if (!isCurrentlyToggled) {
                loveReplyUseCase(replyId)
            } else {
                unloveReplyUseCase(replyId)
            }

            // Handle result
            result.onSuccess {
                // Reset failure count on success
                replyFailureCounts.remove(replyId)
            }.onFailure {
                // Revert optimistic update
                _toggledReplies.update { current ->
                    if (isCurrentlyToggled) {
                        current + replyId
                    } else {
                        current - replyId
                    }
                }

                // Track failures
                val failureCount = replyFailureCounts.getOrDefault(replyId, 0) + 1
                replyFailureCounts[replyId] = failureCount

                // Show appropriate error message
                val message = when {
                    failureCount >= MAX_RETRIES -> "Having trouble connecting. Please check your internet."
                    else -> "Failed to ${if (isCurrentlyToggled) "unlove" else "love"} reply"
                }

                SnackbarController.showError(
                    message = message,
                    actionLabel = "Retry",
                    onRetry = if (failureCount < MAX_RETRIES) {
                        { toggleLoveReply(replyId) }
                    } else null
                )
            }

            loveReplyJobs.remove(replyId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10 // Load more when 10 items from bottom
        private const val MAX_RETRIES = 3
        private const val POLLING_INTERVAL_MS = 30_000L // 30 seconds
    }
}
