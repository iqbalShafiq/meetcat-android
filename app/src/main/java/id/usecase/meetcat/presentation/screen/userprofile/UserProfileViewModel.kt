package id.usecase.meetcat.presentation.screen.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply
import id.usecase.meetcat.domain.model.User
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UserProfileUiEffect>()
    val uiEffect: Flow<UserProfileUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: UserProfileUiEvent) {
        when (event) {
            is UserProfileUiEvent.Refresh -> refresh()
            is UserProfileUiEvent.LoadMore -> loadMore()
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

            // Simulate loading delay
            kotlinx.coroutines.delay(500)

            // Mock data based on userId
            val user = createMockUser(userId)
            val posts = createMockPosts(user)
            val replies = createMockReplies(user)
            val lovedItems = createMockLovedItems()

            _uiState.update {
                it.copy(
                    user = user,
                    posts = posts.toImmutableList(),
                    replies = replies.toImmutableList(),
                    lovedItems = lovedItems.toImmutableList(),
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            // Simulate loading delay
            kotlinx.coroutines.delay(500)

            val user = createMockUser(userId)
            val posts = createMockPosts(user)
            val replies = createMockReplies(user)
            val lovedItems = createMockLovedItems()

            _uiState.update {
                it.copy(
                    user = user,
                    posts = posts.toImmutableList(),
                    replies = replies.toImmutableList(),
                    lovedItems = lovedItems.toImmutableList(),
                    isRefreshing = false,
                    error = null
                )
            }
        }
    }

    private fun loadMore() {
        // Mock pagination - In production, this would:
        // 1. Check if there's more data available (hasMore flag)
        // 2. Prevent duplicate requests (loading flag)
        // 3. Load next page based on current offset/cursor
        // 4. Append new items to existing list
        // Example:
        // if (!_uiState.value.hasMore || _uiState.value.isLoadingMore) return
        // viewModelScope.launch {
        //     _uiState.update { it.copy(isLoadingMore = true) }
        //     val result = repository.getUserPosts(userId, page = currentPage + 1)
        //     result.fold(
        //         onSuccess = { newPosts ->
        //             _uiState.update { state ->
        //                 state.copy(
        //                     posts = (state.posts + newPosts).toImmutableList(),
        //                     currentPage = currentPage + 1,
        //                     hasMore = newPosts.size >= pageSize,
        //                     isLoadingMore = false
        //                 )
        //             }
        //         },
        //         onFailure = { _uiState.update { it.copy(isLoadingMore = false) } }
        //     )
        // }
    }

    private fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun toggleFollow() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    user = state.user?.copy(
                        isFollowing = !state.user.isFollowing,
                        followersCount = if (state.user.isFollowing)
                            state.user.followersCount - 1
                        else
                            state.user.followersCount + 1
                    )
                )
            }
        }
    }

    private fun toggleLovePost(postId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    posts = state.posts.map { post ->
                        if (post.id == postId) {
                            post.copy(
                                isLoved = !post.isLoved,
                                lovesCount = if (post.isLoved) post.lovesCount - 1 else post.lovesCount + 1
                            )
                        } else post
                    }.toImmutableList(),
                    lovedItems = state.lovedItems.map { item ->
                        when (item) {
                            is FeedItem.PostItem -> {
                                if (item.post.id == postId) {
                                    FeedItem.PostItem(
                                        item.post.copy(
                                            isLoved = !item.post.isLoved,
                                            lovesCount = if (item.post.isLoved) item.post.lovesCount - 1 else item.post.lovesCount + 1
                                        )
                                    )
                                } else item
                            }
                            is FeedItem.ReplyItem -> item
                        }
                    }.toImmutableList()
                )
            }
        }
    }

    private fun toggleLoveReply(replyId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    replies = state.replies.map { item ->
                        if (item.reply.id == replyId) {
                            FeedItem.ReplyItem(
                                item.reply.copy(
                                    isLoved = !item.reply.isLoved,
                                    lovesCount = if (item.reply.isLoved) item.reply.lovesCount - 1 else item.reply.lovesCount + 1
                                )
                            )
                        } else item
                    }.toImmutableList(),
                    lovedItems = state.lovedItems.map { item ->
                        when (item) {
                            is FeedItem.ReplyItem -> {
                                if (item.reply.id == replyId) {
                                    FeedItem.ReplyItem(
                                        item.reply.copy(
                                            isLoved = !item.reply.isLoved,
                                            lovesCount = if (item.reply.isLoved) item.reply.lovesCount - 1 else item.reply.lovesCount + 1
                                        )
                                    )
                                } else item
                            }
                            is FeedItem.PostItem -> item
                        }
                    }.toImmutableList()
                )
            }
        }
    }

    // Mock data helpers
    private fun createMockUser(userId: String) = User(
        id = userId,
        username = "user_$userId",
        displayName = when (userId) {
            "1" -> "Cat Lover"
            "2" -> "Meow Master"
            "3" -> "Kitty Kingdom"
            else -> "User $userId"
        },
        bio = "Cat enthusiast and photographer 🐱📸\nLove sharing cat moments!",
        profileImageUrl = "https://picsum.photos/200?random=$userId",
        followersCount = (500..5000).random(),
        followingCount = (100..1000).random(),
        postsCount = (20..200).random(),
        isFollowing = false,
        createdAt = System.currentTimeMillis() - 86400000L * 30
    )

    private fun createMockPosts(user: User): List<Post> = (1..10).map { index ->
        Post(
            id = "${user.id}_post_$index",
            userId = user.id,
            user = user,
            caption = "Post #$index from ${user.displayName}",
            mediaItems = listOf(
                MediaItem.Image(
                    url = "https://picsum.photos/800/600?random=${user.id.hashCode() + index}",
                    thumbnailUrl = "https://picsum.photos/200/150?random=${user.id.hashCode() + index}",
                    width = 800,
                    height = if (index % 3 == 0) 1000 else if (index % 2 == 0) 600 else 800
                )
            ),
            location = if (index % 3 == 0) Location(
                latitude = -6.2088,
                longitude = 106.8456,
                address = "Jakarta, Indonesia",
                name = "Jakarta"
            ) else null,
            lovesCount = (50..500).random(),
            commentsCount = (5..100).random(),
            repliesCount = (2..50).random(),
            isLoved = false,
            createdAt = System.currentTimeMillis() - 3600000L * index
        )
    }

    private fun createMockReplies(user: User): List<FeedItem.ReplyItem> {
        val otherUser = User(
            id = "other_user_${user.id}",
            username = "cat_fan",
            displayName = "Cat Fan",
            bio = "Love all cats 🐱",
            profileImageUrl = "https://picsum.photos/200?random=999",
            followersCount = 1234,
            followingCount = 567,
            postsCount = 89,
            isFollowing = false,
            createdAt = System.currentTimeMillis() - 86400000
        )

        return (1..5).map { index ->
            val originalPost = Post(
                id = "original_${user.id}_$index",
                userId = otherUser.id,
                user = otherUser,
                caption = "Original post about cats",
                mediaItems = listOf(
                    MediaItem.Image(
                        url = "https://picsum.photos/800/600?random=${1000 + index}",
                        thumbnailUrl = "https://picsum.photos/200/150?random=${1000 + index}",
                        width = 800,
                        height = 600
                    )
                ),
                location = null,
                lovesCount = (50..500).random(),
                commentsCount = (5..100).random(),
                repliesCount = (2..50).random(),
                isLoved = false,
                createdAt = System.currentTimeMillis() - 7200000L * index
            )

            FeedItem.ReplyItem(
                Reply(
                    id = "${user.id}_reply_$index",
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    userId = user.id,
                    user = user,
                    text = "Reply #$index from ${user.displayName}",
                    mediaItems = null,
                    location = null,
                    lovesCount = (10..200).random(),
                    commentsCount = (1..50).random(),
                    isLoved = false,
                    createdAt = System.currentTimeMillis() - 5400000L * index
                )
            )
        }
    }

    private fun createMockLovedItems(): List<FeedItem> = emptyList()
}
