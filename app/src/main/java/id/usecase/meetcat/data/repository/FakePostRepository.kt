package id.usecase.meetcat.data.repository

import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.repository.PostRepository
import kotlinx.coroutines.delay

class FakePostRepository : PostRepository {

    private val mockUsers = listOf(
        User(
            id = "1",
            username = "cat_lover_123",
            displayName = "Cat Lover",
            bio = "Love all cats",
            profileImageUrl = "https://picsum.photos/200?random=1",
            followersCount = 1234,
            followingCount = 567,
            postsCount = 89,
            isFollowing = false,
            createdAt = System.currentTimeMillis() - 86400000
        ),
        User(
            id = "2",
            username = "meow_master",
            displayName = "Meow Master",
            bio = "Professional cat photographer",
            profileImageUrl = "https://picsum.photos/200?random=2",
            followersCount = 5678,
            followingCount = 234,
            postsCount = 456,
            isFollowing = true,
            createdAt = System.currentTimeMillis() - 172800000
        ),
        User(
            id = "3",
            username = "kitty_kingdom",
            displayName = "Kitty Kingdom",
            bio = "Cat rescue volunteer",
            profileImageUrl = "https://picsum.photos/200?random=3",
            followersCount = 9012,
            followingCount = 345,
            postsCount = 678,
            isFollowing = false,
            createdAt = System.currentTimeMillis() - 259200000
        )
    )

    private val mockPosts = listOf(
        Post(
            id = "post1",
            userId = "1",
            user = mockUsers[0],
            caption = "Found this adorable cat sleeping in the sun! 🐱☀️",
            mediaItems = listOf(
                MediaItem.Image(
                    url = "https://picsum.photos/800/600?random=10",
                    thumbnailUrl = "https://picsum.photos/200/150?random=10",
                    width = 800,
                    height = 600
                )
            ),
            location = Location(
                latitude = -6.2088,
                longitude = 106.8456,
                address = "Jakarta, Indonesia",
                name = "Jakarta"
            ),
            lovesCount = 234,
            commentsCount = 45,
            repliesCount = 12,
            isLoved = false,
            createdAt = System.currentTimeMillis() - 3600000
        ),
        Post(
            id = "post2",
            userId = "2",
            user = mockUsers[1],
            caption = "My cat just learned a new trick! Watch this amazing jump! 🎪",
            mediaItems = listOf(
                MediaItem.Video(
                    url = "https://picsum.photos/800/600?random=11",
                    thumbnailUrl = "https://picsum.photos/200/150?random=11",
                    duration = 15000,
                    width = 800,
                    height = 600
                )
            ),
            location = null,
            lovesCount = 567,
            commentsCount = 89,
            repliesCount = 23,
            isLoved = true,
            createdAt = System.currentTimeMillis() - 7200000
        ),
        Post(
            id = "post3",
            userId = "3",
            user = mockUsers[2],
            caption = "Rescued this beautiful kitten today. Looking for a forever home! 💕",
            mediaItems = listOf(
                MediaItem.Image(
                    url = "https://picsum.photos/800/600?random=12",
                    thumbnailUrl = "https://picsum.photos/200/150?random=12",
                    width = 800,
                    height = 600
                ),
                MediaItem.Image(
                    url = "https://picsum.photos/800/600?random=13",
                    thumbnailUrl = "https://picsum.photos/200/150?random=13",
                    width = 800,
                    height = 600
                )
            ),
            location = Location(
                latitude = -6.9175,
                longitude = 107.6191,
                address = "Bandung, Indonesia",
                name = "Bandung"
            ),
            lovesCount = 890,
            commentsCount = 123,
            repliesCount = 34,
            isLoved = false,
            createdAt = System.currentTimeMillis() - 10800000
        )
    )

    private val mockReplies = listOf(
        Reply(
            id = "reply1",
            originalPostId = "post1",
            originalPost = mockPosts[0],
            userId = "2",
            user = mockUsers[1],
            text = "This is so cute! I also saw a cat like this yesterday 😍",
            mediaItems = listOf(
                MediaItem.Image(
                    url = "https://picsum.photos/800/600?random=20",
                    thumbnailUrl = "https://picsum.photos/200/150?random=20",
                    width = 800,
                    height = 600
                )
            ),
            location = Location(
                latitude = -6.2088,
                longitude = 106.8456,
                address = "Jakarta, Indonesia",
                name = "Jakarta"
            ),
            lovesCount = 45,
            commentsCount = 12,
            isLoved = false,
            createdAt = System.currentTimeMillis() - 5400000
        ),
        Reply(
            id = "reply2",
            originalPostId = "post2",
            originalPost = mockPosts[1],
            userId = "3",
            user = mockUsers[2],
            text = "Wow! That's incredible! How did you train your cat to do that?",
            mediaItems = null,
            location = null,
            lovesCount = 23,
            commentsCount = 5,
            isLoved = true,
            createdAt = System.currentTimeMillis() - 9000000
        )
    )

    private val mockComments = listOf(
        Comment(
            id = "comment1",
            postId = "post1",
            userId = "2",
            user = mockUsers[1],
            text = "This is such a great photo! 😍",
            lovesCount = 12,
            isLoved = false,
            createdAt = System.currentTimeMillis() - 1800000
        ),
        Comment(
            id = "comment2",
            postId = "post1",
            userId = "3",
            user = mockUsers[2],
            text = "Absolutely adorable! Where did you take this?",
            lovesCount = 25,
            isLoved = true,
            createdAt = System.currentTimeMillis() - 2400000
        ),
        Comment(
            id = "comment3",
            postId = "post2",
            userId = "1",
            user = mockUsers[0],
            text = "Amazing! My cat can't do that yet 😂",
            lovesCount = 8,
            isLoved = false,
            createdAt = System.currentTimeMillis() - 3000000
        ),
        Comment(
            id = "comment4",
            postId = "reply1",
            userId = "1",
            user = mockUsers[0],
            text = "Thanks for your comment! 💕",
            lovesCount = 5,
            isLoved = false,
            createdAt = System.currentTimeMillis() - 3600000
        )
    )

    private val feedItems = listOf(
        FeedItem.PostItem(mockPosts[0]),
        FeedItem.ReplyItem(mockReplies[0]),
        FeedItem.PostItem(mockPosts[1]),
        FeedItem.PostItem(mockPosts[2]),
        FeedItem.ReplyItem(mockReplies[1])
    )

    override suspend fun getExploreFeed(page: Int, pageSize: Int): Result<List<FeedItem>> {
        delay(1000)

        // Simulate paginated feed with generated data
        val allFeedItems = generatePaginatedFeed(totalItems = 100)

        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allFeedItems.size)

        return if (startIndex < allFeedItems.size) {
            Result.success(allFeedItems.subList(startIndex, endIndex))
        } else {
            Result.success(emptyList())
        }
    }

    private fun generatePaginatedFeed(totalItems: Int): List<FeedItem> {
        val items = mutableListOf<FeedItem>()

        for (i in 0 until totalItems) {
            val userIndex = i % mockUsers.size
            val postIndex = i % mockPosts.size
            val replyIndex = i % mockReplies.size

            // Mix posts and replies (60% posts, 40% replies)
            if (i % 5 != 4) {
                // Generate post with unique ID
                val basePost = mockPosts[postIndex]
                val post = basePost.copy(
                    id = "post_page_$i",
                    user = mockUsers[userIndex],
                    userId = mockUsers[userIndex].id,
                    caption = when (i % 6) {
                        0 -> "My cat enjoying the sunny afternoon ☀️🐱 #${i}"
                        1 -> "Caught this cute moment while napping 😴 #${i}"
                        2 -> "Play time is the best time! 🎾 #${i}"
                        3 -> "Look at those beautiful eyes 👀✨ #${i}"
                        4 -> "Another day, another cat photo 📸 #${i}"
                        else -> "Beautiful cat content 🐱💕 #${i}"
                    },
                    lovesCount = (10..1000).random(),
                    commentsCount = (0..200).random(),
                    repliesCount = (0..50).random(),
                    isLoved = false, // Default to not loved
                    createdAt = System.currentTimeMillis() - (3600000L * i)
                )
                items.add(FeedItem.PostItem(post))
            } else {
                // Generate reply with unique ID
                val baseReply = mockReplies[replyIndex]
                val originalPost = mockPosts[postIndex].copy(id = "original_post_$i")
                val reply = baseReply.copy(
                    id = "reply_page_$i",
                    user = mockUsers[userIndex],
                    userId = mockUsers[userIndex].id,
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    text = "Reply #$i - Great content! 💕",
                    lovesCount = (5..500).random(),
                    commentsCount = (0..100).random(),
                    isLoved = false, // Default to not loved
                    createdAt = System.currentTimeMillis() - (3600000L * i)
                )
                items.add(FeedItem.ReplyItem(reply))
            }
        }

        return items
    }

    override suspend fun lovePost(postId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun unlovePost(postId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun loveReply(replyId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun unloveReply(replyId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun getRandomPosts(count: Int): Result<List<Post>> {
        delay(1000)
        // Return shuffled posts with varied aspect ratios, with unique IDs to avoid key duplication
        val shuffledPosts = mockPosts.shuffled()
        val result = mutableListOf<Post>()

        // Varied heights for Instagram-like staggered grid effect
        val heightVariations = listOf(600, 800, 1000, 1200, 650, 900, 750, 1100, 850)

        var index = 0
        while (result.size < count) {
            for (post in shuffledPosts) {
                if (result.size >= count) break

                // Get varied height for this post
                val height = heightVariations[index % heightVariations.size]
                val width = 800 // Keep width constant

                // Create a copy with unique ID and varied aspect ratio
                val uniquePost = post.copy(
                    id = "${post.id}_${index}",
                    mediaItems = post.mediaItems.map { mediaItem ->
                        when (mediaItem) {
                            is MediaItem.Image -> mediaItem.copy(
                                width = width,
                                height = height
                            )
                            is MediaItem.Video -> mediaItem.copy(
                                width = width,
                                height = height
                            )
                        }
                    }
                )
                result.add(uniquePost)
                index++
            }
        }

        return Result.success(result)
    }

    override suspend fun searchPosts(
        query: String,
        page: Int,
        pageSize: Int
    ): Result<List<Post>> {
        delay(500)

        // Simple search by caption
        val results = mockPosts.filter { post ->
            post.caption.contains(query, ignoreCase = true) ||
            post.user.username.contains(query, ignoreCase = true) ||
            post.user.displayName.contains(query, ignoreCase = true)
        }

        return Result.success(results)
    }

    override suspend fun getNearbyPosts(
        location: Location,
        radiusKm: Double,
        limit: Int
    ): Result<List<Post>> {
        delay(1000)

        // Filter posts that have location and are within radius
        val nearbyPosts = mockPosts.filter { post ->
            post.location != null && isWithinRadius(
                lat1 = location.latitude,
                lon1 = location.longitude,
                lat2 = post.location.latitude,
                lon2 = post.location.longitude,
                radiusKm = radiusKm
            )
        }.take(limit)

        // Generate more posts with random locations near the current location for testing
        val generatedPosts = List(10) { index ->
            val basePost = mockPosts[index % mockPosts.size]
            val randomLatOffset = (Math.random() - 0.5) * 0.1 // ±0.05 degrees (~5.5km)
            val randomLonOffset = (Math.random() - 0.5) * 0.1

            basePost.copy(
                id = "nearby_${index}",
                location = Location(
                    latitude = location.latitude + randomLatOffset,
                    longitude = location.longitude + randomLonOffset,
                    address = "Nearby Location $index",
                    name = "Cat Spot $index"
                )
            )
        }

        return Result.success(nearbyPosts + generatedPosts)
    }

    private fun isWithinRadius(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        radiusKm: Double
    ): Boolean {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = earthRadiusKm * c

        return distance <= radiusKm
    }

    override suspend fun getPostById(postId: String): Result<Post> {
        delay(500)

        // First try to find in hardcoded mockPosts
        val post = mockPosts.find { it.id == postId }
        if (post != null) {
            return Result.success(post)
        }

        // Handle paginated posts (post_page_X format)
        if (postId.startsWith("post_page_")) {
            val pageNum = postId.substringAfter("post_page_").toIntOrNull()
            if (pageNum != null) {
                val userIndex = pageNum % mockUsers.size
                val postIndex = pageNum % mockPosts.size
                val basePost = mockPosts[postIndex]

                val generatedPost = basePost.copy(
                    id = postId,
                    user = mockUsers[userIndex],
                    userId = mockUsers[userIndex].id,
                    caption = when (pageNum % 6) {
                        0 -> "My cat enjoying the sunny afternoon ☀️🐱 #${pageNum}"
                        1 -> "Caught this cute moment while napping 😴 #${pageNum}"
                        2 -> "Play time is the best time! 🎾 #${pageNum}"
                        3 -> "Look at those beautiful eyes 👀✨ #${pageNum}"
                        4 -> "Another day, another cat photo 📸 #${pageNum}"
                        else -> "Beautiful cat content 🐱💕 #${pageNum}"
                    },
                    lovesCount = (10..1000).random(),
                    commentsCount = (0..200).random(),
                    repliesCount = (0..50).random(),
                    isLoved = false,
                    createdAt = System.currentTimeMillis() - (3600000L * pageNum)
                )
                return Result.success(generatedPost)
            }
        }

        // Handle original posts from replies (original_post_X format)
        if (postId.startsWith("original_post_")) {
            val pageNum = postId.substringAfter("original_post_").toIntOrNull()
            if (pageNum != null) {
                val postIndex = pageNum % mockPosts.size
                val generatedPost = mockPosts[postIndex].copy(id = postId)
                return Result.success(generatedPost)
            }
        }

        return Result.failure(Exception("Post not found"))
    }

    override suspend fun getReplyById(replyId: String): Result<Reply> {
        delay(500)

        // First try to find in hardcoded mockReplies
        val reply = mockReplies.find { it.id == replyId }
        if (reply != null) {
            return Result.success(reply)
        }

        // Handle paginated replies (reply_page_X format)
        if (replyId.startsWith("reply_page_")) {
            val pageNum = replyId.substringAfter("reply_page_").toIntOrNull()
            if (pageNum != null) {
                val userIndex = pageNum % mockUsers.size
                val replyIndex = pageNum % mockReplies.size
                val postIndex = pageNum % mockPosts.size
                val baseReply = mockReplies[replyIndex]

                val originalPost = mockPosts[postIndex].copy(id = "original_post_$pageNum")
                val generatedReply = baseReply.copy(
                    id = replyId,
                    user = mockUsers[userIndex],
                    userId = mockUsers[userIndex].id,
                    originalPostId = originalPost.id,
                    originalPost = originalPost,
                    text = "Reply #$pageNum - Great content! 💕",
                    lovesCount = (5..500).random(),
                    commentsCount = (0..100).random(),
                    isLoved = false,
                    createdAt = System.currentTimeMillis() - (3600000L * pageNum)
                )
                return Result.success(generatedReply)
            }
        }

        return Result.failure(Exception("Reply not found"))
    }

    override suspend fun getPostComments(postId: String): Result<List<Comment>> {
        delay(500)

        // First try to find in hardcoded mockComments
        val existingComments = mockComments.filter { it.postId == postId }
        if (existingComments.isNotEmpty()) {
            return Result.success(existingComments)
        }

        // Generate mock comments for paginated posts
        if (postId.startsWith("post_page_") || postId.startsWith("original_post_")) {
            val numComments = (0..5).random() // Random 0-5 comments
            val comments = (0 until numComments).map { i ->
                val userIndex = i % mockUsers.size
                Comment(
                    id = "comment_${postId}_$i",
                    postId = postId,
                    userId = mockUsers[userIndex].id,
                    user = mockUsers[userIndex],
                    text = when (i % 6) {
                        0 -> "This is such a great photo! 😍"
                        1 -> "Absolutely adorable! 🐱💕"
                        2 -> "Amazing content! Love it! ✨"
                        3 -> "So cute! Where did you take this?"
                        4 -> "Beautiful! Thanks for sharing! 📸"
                        else -> "Great post! Keep it up! 👍"
                    },
                    lovesCount = (0..50).random(),
                    isLoved = false,
                    createdAt = System.currentTimeMillis() - (1800000L * (i + 1))
                )
            }
            return Result.success(comments)
        }

        // No comments for this post
        return Result.success(emptyList())
    }

    override suspend fun getReplyComments(replyId: String): Result<List<Comment>> {
        delay(500)
        val comments = mockComments.filter { it.postId == replyId }
        return Result.success(comments)
    }

    override suspend fun loveComment(commentId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    override suspend fun unloveComment(commentId: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }

    /**
     * Reset repository state for test isolation.
     * Note: This repository is stateless, so reset() is provided for consistency
     * with other Fake repositories. Future stateful features can use this method.
     */
    fun reset() {
        // Currently stateless - no state to reset
        // Add state clearing here if mutable state is added in the future
    }
}
