package id.usecase.meetcat.presentation.screen.profile

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

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ProfileUiEffect>()
    val uiEffect: Flow<ProfileUiEffect> = _uiEffect.receiveAsFlow()

    init {
        loadProfileData()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.Refresh -> refresh()
            is ProfileUiEvent.LoadMore -> loadMore()
            is ProfileUiEvent.TabSelected -> selectTab(event.tab)
            is ProfileUiEvent.NavigateToPost -> {
                viewModelScope.launch {
                    _uiEffect.send(ProfileUiEffect.NavigateToPost(event.postId))
                }
            }
            is ProfileUiEvent.LovePost -> toggleLovePost(event.postId)
            is ProfileUiEvent.LoveReply -> toggleLoveReply(event.replyId)
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate loading delay
            kotlinx.coroutines.delay(500)

            // Mock data
            val mockUser = createMockUser()
            val mockPosts = createMockPosts(mockUser)
            val mockReplies = createMockReplies(mockUser)
            val mockLovedItems = createMockLovedItems()

            _uiState.update {
                it.copy(
                    user = mockUser,
                    posts = mockPosts.toImmutableList(),
                    replies = mockReplies.toImmutableList(),
                    lovedItems = mockLovedItems.toImmutableList(),
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

            // Reload mock data
            val mockUser = createMockUser()
            val mockPosts = createMockPosts(mockUser)
            val mockReplies = createMockReplies(mockUser)
            val mockLovedItems = createMockLovedItems()

            _uiState.update {
                it.copy(
                    user = mockUser,
                    posts = mockPosts.toImmutableList(),
                    replies = mockReplies.toImmutableList(),
                    lovedItems = mockLovedItems.toImmutableList(),
                    isRefreshing = false,
                    error = null
                )
            }
        }
    }

    private fun loadMore() {
        // TODO: Implement pagination
    }

    private fun selectTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
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
    private fun createMockUser() = User(
        id = "current_user",
        username = "catwhiskerer",
        displayName = "Cat Whisperer",
        bio = "Passionate about cats and photography 🐱📸\nLiving my best life with 3 adorable felines",
        profileImageUrl = "https://picsum.photos/200?random=100",
        followersCount = 2456,
        followingCount = 342,
        postsCount = 156,
        isFollowing = false,
        createdAt = System.currentTimeMillis() - 86400000L * 30
    )

    private fun createMockPosts(user: User): List<Post> = (1..12).map { index ->
        Post(
            id = "post_$index",
            userId = user.id,
            user = user,
            caption = when (index % 5) {
                0 -> "My cat enjoying the sunny afternoon ☀️🐱"
                1 -> "Caught this cute moment while napping 😴"
                2 -> "Play time is the best time! 🎾"
                3 -> "Look at those beautiful eyes 👀✨"
                else -> "Another day, another cat photo 📸"
            },
            mediaItems = listOf(
                MediaItem.Image(
                    url = "https://picsum.photos/800/600?random=$index",
                    thumbnailUrl = "https://picsum.photos/200/150?random=$index",
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
            isLoved = index % 4 == 0,
            createdAt = System.currentTimeMillis() - 3600000L * index
        )
    }

    private fun createMockReplies(user: User): List<FeedItem.ReplyItem> {
        val otherUser = User(
            id = "other_user",
            username = "cat_lover_123",
            displayName = "Cat Lover",
            bio = "Love all cats 🐱",
            profileImageUrl = "https://picsum.photos/200?random=200",
            followersCount = 1234,
            followingCount = 567,
            postsCount = 89,
            isFollowing = false,
            createdAt = System.currentTimeMillis() - 86400000
        )

        return (1..8).map { index ->
            val originalPost = Post(
                id = "original_post_$index",
                userId = otherUser.id,
                user = otherUser,
                caption = "Original post about cats",
                mediaItems = listOf(
                    MediaItem.Image(
                        url = "https://picsum.photos/800/600?random=${100 + index}",
                        thumbnailUrl = "https://picsum.photos/200/150?random=${100 + index}",
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
                    id = "reply_$index",
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    userId = user.id,
                    user = user,
                    text = when (index % 4) {
                        0 -> "This is so adorable! I love it! 😍"
                        1 -> "What a beautiful cat! How old is it?"
                        2 -> "I have a cat just like this one! 🐱"
                        else -> "Amazing photo! Keep posting more!"
                    },
                    mediaItems = if (index % 3 == 0) listOf(
                        MediaItem.Image(
                            url = "https://picsum.photos/800/600?random=${200 + index}",
                            thumbnailUrl = "https://picsum.photos/200/150?random=${200 + index}",
                            width = 800,
                            height = 600
                        )
                    ) else null,
                    location = null,
                    lovesCount = (10..200).random(),
                    commentsCount = (1..50).random(),
                    isLoved = index % 3 == 0,
                    createdAt = System.currentTimeMillis() - 5400000L * index
                )
            )
        }
    }

    private fun createMockLovedItems(): List<FeedItem> {
        val otherUser1 = User(
            id = "other_user_1",
            username = "meow_master",
            displayName = "Meow Master",
            bio = "Professional cat photographer",
            profileImageUrl = "https://picsum.photos/200?random=201",
            followersCount = 5678,
            followingCount = 234,
            postsCount = 456,
            isFollowing = true,
            createdAt = System.currentTimeMillis() - 172800000
        )

        val otherUser2 = User(
            id = "other_user_2",
            username = "kitty_fan",
            displayName = "Kitty Fan",
            bio = "Cats are life ❤️",
            profileImageUrl = "https://picsum.photos/200?random=202",
            followersCount = 3456,
            followingCount = 123,
            postsCount = 234,
            isFollowing = false,
            createdAt = System.currentTimeMillis() - 259200000
        )

        val posts = (1..5).map { index ->
            FeedItem.PostItem(
                Post(
                    id = "loved_post_$index",
                    userId = otherUser1.id,
                    user = otherUser1,
                    caption = "Loved post #$index - Beautiful cat content!",
                    mediaItems = listOf(
                        MediaItem.Image(
                            url = "https://picsum.photos/800/600?random=${300 + index}",
                            thumbnailUrl = "https://picsum.photos/200/150?random=${300 + index}",
                            width = 800,
                            height = 600
                        )
                    ),
                    location = null,
                    lovesCount = (100..1000).random(),
                    commentsCount = (10..200).random(),
                    repliesCount = (5..100).random(),
                    isLoved = true,
                    createdAt = System.currentTimeMillis() - 10800000L * index
                )
            )
        }

        val replies = (1..5).map { index ->
            val originalPost = Post(
                id = "loved_original_post_$index",
                userId = otherUser2.id,
                user = otherUser2,
                caption = "Original post for loved reply",
                mediaItems = listOf(
                    MediaItem.Image(
                        url = "https://picsum.photos/800/600?random=${400 + index}",
                        thumbnailUrl = "https://picsum.photos/200/150?random=${400 + index}",
                        width = 800,
                        height = 600
                    )
                ),
                location = null,
                lovesCount = (50..500).random(),
                commentsCount = (5..100).random(),
                repliesCount = (2..50).random(),
                isLoved = false,
                createdAt = System.currentTimeMillis() - 14400000L * index
            )

            FeedItem.ReplyItem(
                Reply(
                    id = "loved_reply_$index",
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    userId = otherUser1.id,
                    user = otherUser1,
                    text = "Loved reply #$index - Great content!",
                    mediaItems = null,
                    location = null,
                    lovesCount = (20..300).random(),
                    commentsCount = (2..80).random(),
                    isLoved = true,
                    createdAt = System.currentTimeMillis() - 12600000L * index
                )
            )
        }

        return (posts + replies).shuffled()
    }
}
