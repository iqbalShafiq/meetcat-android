package id.usecase.meetcat.presentation.screen.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.paging.UserLovedItemsPagingSource
import id.usecase.meetcat.domain.paging.UserPostsPagingSource
import id.usecase.meetcat.domain.paging.UserRepliesPagingSource
import id.usecase.meetcat.domain.repository.PostRepository
import id.usecase.meetcat.domain.repository.UserRepository
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
 * ViewModel for UserProfileScreen with Paging3 support for 3 tabs (Posts, Replies, Loved)
 *
 * Similar to ProfileViewModel but for viewing OTHER users' profiles (not current user)
 *
 * Clean & Testable:
 * - Uses repository interfaces (no direct Android dependencies)
 * - Separate concerns: User data loading vs Pagination
 * - Optimistic UI updates with proper rollback on failure
 * - Each tab has its own PagingData flow
 */
class UserProfileViewModel(
    private val userId: String,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getUserRepliesUseCase: GetUserRepliesUseCase,
    private val getUserLovedItemsUseCase: GetUserLovedItemsUseCase,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UserProfileUiEffect>()
    val uiEffect: Flow<UserProfileUiEffect> = _uiEffect.receiveAsFlow()

    // Track love/unlove toggles for optimistic UI updates across all tabs
    private val _toggledLoves = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Track follow/unfollow for optimistic UI update
    private val _isFollowingToggled = MutableStateFlow<Boolean?>(null)

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
                    userId = userId,
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
                    userId = userId,
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
                    userId = userId,
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
                    ),
                    parentPost = replyItem.parentPost
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
                            ),
                            parentPost = item.parentPost
                        )
                    } ?: item
                }
            }
        }
    }

    init {
        loadUserProfile()
    }

    fun onEvent(event: UserProfileUiEvent) {
        when (event) {
            is UserProfileUiEvent.Refresh -> {
                // Refresh is handled by LazyPagingItems.refresh() in UI layer
            }
            is UserProfileUiEvent.LoadMore -> {
                // LoadMore is handled automatically by Paging3
            }
            is UserProfileUiEvent.TabSelected -> selectTab(event.tab)
            is UserProfileUiEvent.NavigateToPost -> {
                viewModelScope.launch {
                    _uiEffect.send(UserProfileUiEffect.NavigateToPost(event.postId))
                }
            }
            is UserProfileUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(UserProfileUiEffect.NavigateBack)
                }
            }
            is UserProfileUiEvent.LovePost -> toggleLovePost(event.postId)
            is UserProfileUiEvent.LoveReply -> toggleLoveReply(event.replyId)
            is UserProfileUiEvent.ToggleFollow -> toggleFollow()
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // In a real app, this would fetch user from repository
            // For now, using mock data from FakeUserRepository via follow/unfollow
            // The actual user data will come from the repository when we have a GetUserUseCase

            // Simulate loading - in production this would be getUserUseCase(userId)
            kotlinx.coroutines.delay(500)

            // Mock user data (this would come from repository in production)
            val mockUser = User(
                id = userId,
                username = "user_$userId",
                displayName = "User ${userId.take(5)}",
                bio = "This is a user profile",
                profileImageUrl = null,
                followersCount = 150,
                followingCount = 75,
                postsCount = 42,
                isFollowing = false,
                createdAt = System.currentTimeMillis() - 86400000L * 30
            )

            _uiState.update {
                it.copy(
                    user = mockUser,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun selectTab(tab: UserProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun toggleFollow() {
        viewModelScope.launch {
            val currentUser = _uiState.value.user ?: return@launch
            val currentFollowState = _isFollowingToggled.value ?: currentUser.isFollowing

            // Determine new state
            val newFollowState = !currentFollowState

            // Apply optimistic update
            _isFollowingToggled.value = newFollowState
            _uiState.update { state ->
                state.copy(
                    user = state.user?.copy(
                        isFollowing = newFollowState,
                        followersCount = if (newFollowState) {
                            state.user.followersCount + 1
                        } else {
                            state.user.followersCount - 1
                        }
                    )
                )
            }

            // Make API call
            val result = if (newFollowState) {
                userRepository.followUser(userId)
            } else {
                userRepository.unfollowUser(userId)
            }

            // Revert on failure
            result.onFailure {
                _isFollowingToggled.value = currentFollowState
                _uiState.update { state ->
                    state.copy(
                        user = state.user?.copy(
                            isFollowing = currentFollowState,
                            followersCount = if (currentFollowState) {
                                state.user.followersCount + 1
                            } else {
                                state.user.followersCount - 1
                            }
                        )
                    )
                }
                _uiEffect.send(UserProfileUiEffect.ShowError("Failed to update follow status"))
            }
        }
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
                _uiEffect.send(UserProfileUiEffect.ShowError("Failed to update love status"))
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
                _uiEffect.send(UserProfileUiEffect.ShowError("Failed to update love status"))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10
    }
}
