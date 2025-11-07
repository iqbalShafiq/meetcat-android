package id.usecase.meetcat.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.paging.UserLovedItemsPagingSource
import id.usecase.meetcat.domain.paging.UserPostsPagingSource
import id.usecase.meetcat.domain.paging.UserRepliesPagingSource
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.domain.usecase.auth.GetCurrentUserUseCase
import id.usecase.meetcat.domain.usecase.user.GetUserLovedItemsUseCase
import id.usecase.meetcat.domain.usecase.user.GetUserPostsUseCase
import id.usecase.meetcat.domain.usecase.user.GetUserRepliesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for ProfileScreen with Paging3 support for 3 tabs (Posts, Replies, Loved)
 *
 * Clean & Testable:
 * - Uses repository interfaces (no direct Android dependencies)
 * - Separate concerns: User data loading vs Pagination
 * - Optimistic UI updates with proper rollback on failure
 * - Each tab has its own PagingData flow
 */
class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getUserRepliesUseCase: GetUserRepliesUseCase,
    private val getUserLovedItemsUseCase: GetUserLovedItemsUseCase,
    private val postRepository: PostRepository
) : ViewModel(), ScrollableViewModel {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>()
    val uiEffect: Flow<ProfileUiEffect> = _uiEffect.receiveAsFlow()

    // Preserve scroll positions across navigation for each tab
    override var postsScrollIndex: Int = 0
    override var repliesScrollIndex: Int = 0
    override var lovedScrollIndex: Int = 0

    // Track love/unlove toggles for optimistic UI updates across all tabs
    private val _toggledLoves = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Current user ID (loaded in init)
    private var currentUserId: String? = null

    // Paging3 flow for Posts tab
    private val postsFlow: Flow<PagingData<Post>> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserPostsPagingSource(
                    userId = currentUserId ?: "",
                    getUserPostsUseCase = getUserPostsUseCase
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    // Paging3 flow for Replies tab
    private val repliesFlow: Flow<PagingData<FeedItem.ReplyItem>> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserRepliesPagingSource(
                    userId = currentUserId ?: "",
                    getUserRepliesUseCase = getUserRepliesUseCase
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    // Paging3 flow for Loved tab
    private val lovedItemsFlow: Flow<PagingData<FeedItem>> by lazy {
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserLovedItemsPagingSource(
                    userId = currentUserId ?: "",
                    getUserLovedItemsUseCase = getUserLovedItemsUseCase
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    // Posts with optimistic love toggles applied
    val posts: Flow<PagingData<Post>> = combine(
        postsFlow,
        _toggledLoves
    ) { pagingData, toggles ->
        pagingData.map { post ->
            toggles[post.id]?.let { isLoved ->
                post.copy(
                    isLoved = isLoved,
                    lovesCount = if (isLoved) post.lovesCount + 1 else post.lovesCount - 1
                )
            } ?: post
        }
    }

    // Replies with optimistic love toggles applied
    val replies: Flow<PagingData<FeedItem.ReplyItem>> = combine(
        repliesFlow,
        _toggledLoves
    ) { pagingData, toggles ->
        pagingData.map { replyItem ->
            toggles[replyItem.reply.id]?.let { isLoved ->
                FeedItem.ReplyItem(
                    reply = replyItem.reply.copy(
                        isLoved = isLoved,
                        lovesCount = if (isLoved) replyItem.reply.lovesCount + 1 else replyItem.reply.lovesCount - 1
                    )
                )
            } ?: replyItem
        }
    }

    // Loved items with optimistic love toggles applied
    val lovedItems: Flow<PagingData<FeedItem>> = combine(
        lovedItemsFlow,
        _toggledLoves
    ) { pagingData, toggles ->
        pagingData.map { item ->
            when (item) {
                is FeedItem.PostItem -> {
                    toggles[item.post.id]?.let { isLoved ->
                        FeedItem.PostItem(
                            post = item.post.copy(
                                isLoved = isLoved,
                                lovesCount = if (isLoved) item.post.lovesCount + 1 else item.post.lovesCount - 1
                            )
                        )
                    } ?: item
                }
                is FeedItem.ReplyItem -> {
                    toggles[item.reply.id]?.let { isLoved ->
                        FeedItem.ReplyItem(
                            reply = item.reply.copy(
                                isLoved = isLoved,
                                lovesCount = if (isLoved) item.reply.lovesCount + 1 else item.reply.lovesCount - 1
                            )
                        )
                    } ?: item
                }
            }
        }
    }

    init {
        loadCurrentUser()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.Refresh -> {
                // Refresh is handled by LazyPagingItems.refresh() in UI layer
            }
            is ProfileUiEvent.LoadMore -> {
                // LoadMore is handled automatically by Paging3
            }
            is ProfileUiEvent.TabSelected -> selectTab(event.tab)
            is ProfileUiEvent.NavigateToPost -> {
                viewModelScope.launch {
                    _uiEffect.send(ProfileUiEffect.NavigateToPost(event.postId))
                }
            }
            is ProfileUiEvent.LovePost -> toggleLovePost(event.postId)
            is ProfileUiEvent.LoveReply -> toggleLoveReply(event.replyId)
            is ProfileUiEvent.NavigateToSettings -> {
                viewModelScope.launch {
                    _uiEffect.send(ProfileUiEffect.NavigateToSettings)
                }
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val authUser = getCurrentUserUseCase()

                if (authUser != null) {
                    // Convert AuthUser to User
                    // Note: This is a simplified conversion. In a real app, you'd fetch the full User object
                    val user = id.usecase.meetcat.domain.model.User(
                        id = authUser.id,
                        username = authUser.username,
                        displayName = authUser.displayName,
                        bio = null,
                        profileImageUrl = authUser.profileImageUrl,
                        followersCount = 0,
                        followingCount = 0,
                        postsCount = 0,
                        isFollowing = false,
                        createdAt = System.currentTimeMillis()
                    )

                    currentUserId = user.id
                    _uiState.update {
                        it.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    val errorMessage = "Failed to load profile"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    _uiEffect.send(ProfileUiEffect.ShowError(errorMessage))
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Failed to load profile"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                _uiEffect.send(ProfileUiEffect.ShowError(errorMessage))
            }
        }
    }

    private fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun toggleLovePost(postId: String) {
        viewModelScope.launch {
            // Get current state from toggles or assume original state
            val currentToggles = _toggledLoves.value
            val currentLoveState = currentToggles[postId]

            // Determine new state (toggle)
            val newLoveState = when (currentLoveState) {
                null -> true // Not toggled yet, assume was false, toggle to true
                true -> false
                false -> true
            }

            // Apply optimistic update
            _toggledLoves.update { current ->
                current + (postId to newLoveState)
            }

            // Make API call
            val result = if (newLoveState) {
                postRepository.lovePost(postId)
            } else {
                postRepository.unlovePost(postId)
            }

            // Revert on failure
            result.onFailure {
                _toggledLoves.update { current ->
                    if (currentLoveState == null) {
                        current - postId
                    } else {
                        current + (postId to currentLoveState)
                    }
                }
                _uiEffect.send(ProfileUiEffect.ShowError("Failed to update love status"))
            }
        }
    }

    private fun toggleLoveReply(replyId: String) {
        viewModelScope.launch {
            // Get current state from toggles or assume original state
            val currentToggles = _toggledLoves.value
            val currentLoveState = currentToggles[replyId]

            // Determine new state (toggle)
            val newLoveState = when (currentLoveState) {
                null -> true // Not toggled yet, assume was false, toggle to true
                true -> false
                false -> true
            }

            // Apply optimistic update
            _toggledLoves.update { current ->
                current + (replyId to newLoveState)
            }

            // Make API call
            val result = if (newLoveState) {
                postRepository.loveReply(replyId)
            } else {
                postRepository.unloveReply(replyId)
            }

            // Revert on failure
            result.onFailure {
                _toggledLoves.update { current ->
                    if (currentLoveState == null) {
                        current - replyId
                    } else {
                        current + (replyId to currentLoveState)
                    }
                }
                _uiEffect.send(ProfileUiEffect.ShowError("Failed to update love status"))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10
    }
}
