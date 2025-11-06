package id.usecase.meetcat.data.repository

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.repository.UserRepository
import kotlinx.coroutines.delay

/**
 * Fake implementation of UserRepository for testing and development.
 * Generates paginated mock data for user profiles, posts, followers, etc.
 *
 * Features:
 * - No Android dependencies (fully unit testable)
 * - Generates realistic paginated data
 * - Test isolation via reset() method
 */
class FakeUserRepository : UserRepository {

    override suspend fun getUserPosts(userId: String, page: Int, pageSize: Int): Result<List<Post>> {
        delay(800)

        val allPosts = generateUserPosts(userId, totalItems = 50)
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allPosts.size)

        return if (startIndex < allPosts.size) {
            Result.success(allPosts.subList(startIndex, endIndex))
        } else {
            Result.success(emptyList())
        }
    }

    override suspend fun getUserReplies(userId: String, page: Int, pageSize: Int): Result<List<FeedItem.ReplyItem>> {
        delay(800)

        val allReplies = generateUserReplies(userId, totalItems = 30)
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allReplies.size)

        return if (startIndex < allReplies.size) {
            Result.success(allReplies.subList(startIndex, endIndex))
        } else {
            Result.success(emptyList())
        }
    }

    override suspend fun getUserLovedItems(userId: String, page: Int, pageSize: Int): Result<List<FeedItem>> {
        delay(800)

        val allLovedItems = generateUserLovedItems(userId, totalItems = 40)
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allLovedItems.size)

        return if (startIndex < allLovedItems.size) {
            Result.success(allLovedItems.subList(startIndex, endIndex))
        } else {
            Result.success(emptyList())
        }
    }

    override suspend fun getFollowers(userId: String, page: Int, pageSize: Int): Result<List<User>> {
        delay(800)

        val allFollowers = generateFollowers(userId, totalItems = 60)
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allFollowers.size)

        return if (startIndex < allFollowers.size) {
            Result.success(allFollowers.subList(startIndex, endIndex))
        } else {
            Result.success(emptyList())
        }
    }

    override suspend fun getFollowing(userId: String, page: Int, pageSize: Int): Result<List<User>> {
        delay(800)

        val allFollowing = generateFollowing(userId, totalItems = 45)
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allFollowing.size)

        return if (startIndex < allFollowing.size) {
            Result.success(allFollowing.subList(startIndex, endIndex))
        } else {
            Result.success(emptyList())
        }
    }

    override suspend fun followUser(userId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    // Generate mock data methods

    private fun generateUserPosts(userId: String, totalItems: Int): List<Post> {
        val user = createMockUser(userId)
        return (0 until totalItems).map { index ->
            Post(
                id = "user_${userId}_post_$index",
                userId = userId,
                user = user,
                caption = when (index % 6) {
                    0 -> "Beautiful sunset today 🌅 #${index}"
                    1 -> "Coffee time ☕️ #${index}"
                    2 -> "New photo from my collection #${index}"
                    3 -> "Weekend vibes 🎉 #${index}"
                    4 -> "Just posted! Check it out #${index}"
                    else -> "Another awesome post #${index}"
                },
                mediaItems = listOf(
                    MediaItem.Image(
                        url = "https://picsum.photos/800/${if (index % 3 == 0) 1000 else 600}?random=${userId.hashCode() + index}",
                        thumbnailUrl = "https://picsum.photos/200/150?random=${userId.hashCode() + index}",
                        width = 800,
                        height = if (index % 3 == 0) 1000 else 600
                    )
                ),
                location = if (index % 4 == 0) Location(
                    latitude = -6.2088 + (index * 0.01),
                    longitude = 106.8456 + (index * 0.01),
                    address = "Location $index",
                    name = "Place $index"
                ) else null,
                lovesCount = (10..1000).random(),
                commentsCount = (0..200).random(),
                repliesCount = (0..50).random(),
                isLoved = false,
                createdAt = System.currentTimeMillis() - (3600000L * index)
            )
        }
    }

    private fun generateUserReplies(userId: String, totalItems: Int): List<FeedItem.ReplyItem> {
        val user = createMockUser(userId)
        return (0 until totalItems).map { index ->
            val originalUser = createMockUser("other_user_$index")
            val originalPost = Post(
                id = "original_post_for_reply_$index",
                userId = originalUser.id,
                user = originalUser,
                caption = "Original post content $index",
                mediaItems = listOf(
                    MediaItem.Image(
                        url = "https://picsum.photos/800/600?random=${2000 + index}",
                        thumbnailUrl = "https://picsum.photos/200/150?random=${2000 + index}",
                        width = 800,
                        height = 600
                    )
                ),
                location = null,
                lovesCount = (50..500).random(),
                commentsCount = (5..100).random(),
                repliesCount = (2..50).random(),
                isLoved = false,
                createdAt = System.currentTimeMillis() - (7200000L * index)
            )

            FeedItem.ReplyItem(
                Reply(
                    id = "user_${userId}_reply_$index",
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    userId = userId,
                    user = user,
                    text = when (index % 4) {
                        0 -> "Great post! Love it 😍 #${index}"
                        1 -> "This is awesome! 🔥 #${index}"
                        2 -> "Nice work! 👏 #${index}"
                        else -> "Reply number $index"
                    },
                    mediaItems = if (index % 3 == 0) listOf(
                        MediaItem.Image(
                            url = "https://picsum.photos/800/600?random=${3000 + index}",
                            thumbnailUrl = "https://picsum.photos/200/150?random=${3000 + index}",
                            width = 800,
                            height = 600
                        )
                    ) else null,
                    location = null,
                    lovesCount = (5..300).random(),
                    commentsCount = (0..80).random(),
                    isLoved = false,
                    createdAt = System.currentTimeMillis() - (5400000L * index)
                )
            )
        }
    }

    private fun generateUserLovedItems(userId: String, totalItems: Int): List<FeedItem> {
        val posts = (0 until totalItems / 2).map { index ->
            val author = createMockUser("author_$index")
            FeedItem.PostItem(
                Post(
                    id = "loved_post_$index",
                    userId = author.id,
                    user = author,
                    caption = "Loved post #$index",
                    mediaItems = listOf(
                        MediaItem.Image(
                            url = "https://picsum.photos/800/600?random=${4000 + index}",
                            thumbnailUrl = "https://picsum.photos/200/150?random=${4000 + index}",
                            width = 800,
                            height = 600
                        )
                    ),
                    location = null,
                    lovesCount = (100..1000).random(),
                    commentsCount = (10..200).random(),
                    repliesCount = (5..100).random(),
                    isLoved = true,
                    createdAt = System.currentTimeMillis() - (10800000L * index)
                )
            )
        }

        val replies = (0 until totalItems / 2).map { index ->
            val author = createMockUser("reply_author_$index")
            val originalAuthor = createMockUser("original_author_$index")
            val originalPost = Post(
                id = "loved_original_post_$index",
                userId = originalAuthor.id,
                user = originalAuthor,
                caption = "Original post",
                mediaItems = emptyList(),
                location = null,
                lovesCount = 50,
                commentsCount = 10,
                repliesCount = 5,
                isLoved = false,
                createdAt = System.currentTimeMillis() - (14400000L * index)
            )

            FeedItem.ReplyItem(
                Reply(
                    id = "loved_reply_$index",
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    userId = author.id,
                    user = author,
                    text = "Loved reply #$index",
                    mediaItems = null,
                    location = null,
                    lovesCount = (20..300).random(),
                    commentsCount = (2..80).random(),
                    isLoved = true,
                    createdAt = System.currentTimeMillis() - (12600000L * index)
                )
            )
        }

        return (posts + replies).shuffled()
    }

    private fun generateFollowers(userId: String, totalItems: Int): List<User> {
        return (0 until totalItems).map { index ->
            User(
                id = "follower_${userId}_$index",
                username = "follower_$index",
                displayName = "Follower ${index + 1}",
                bio = "Bio of follower $index",
                profileImageUrl = "https://picsum.photos/200?random=${5000 + index}",
                followersCount = (100..5000).random(),
                followingCount = (50..1000).random(),
                postsCount = (10..500).random(),
                isFollowing = index % 3 == 0,
                createdAt = System.currentTimeMillis() - (86400000L * index)
            )
        }
    }

    private fun generateFollowing(userId: String, totalItems: Int): List<User> {
        return (0 until totalItems).map { index ->
            User(
                id = "following_${userId}_$index",
                username = "following_$index",
                displayName = "Following ${index + 1}",
                bio = "Bio of following $index",
                profileImageUrl = "https://picsum.photos/200?random=${6000 + index}",
                followersCount = (100..5000).random(),
                followingCount = (50..1000).random(),
                postsCount = (10..500).random(),
                isFollowing = true,
                createdAt = System.currentTimeMillis() - (86400000L * index)
            )
        }
    }

    private fun createMockUser(userId: String) = User(
        id = userId,
        username = "user_$userId",
        displayName = "User ${userId.take(5)}",
        bio = "Bio for user $userId",
        profileImageUrl = "https://picsum.photos/200?random=${userId.hashCode()}",
        followersCount = (500..5000).random(),
        followingCount = (100..1000).random(),
        postsCount = (20..200).random(),
        isFollowing = false,
        createdAt = System.currentTimeMillis() - 86400000L * 30
    )

    /**
     * Reset repository state for test isolation.
     * Note: This repository is stateless, so reset() is provided for consistency
     */
    fun reset() {
        // Currently stateless - no state to reset
    }
}
