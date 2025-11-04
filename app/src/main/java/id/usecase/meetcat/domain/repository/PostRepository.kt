package id.usecase.meetcat.domain.repository

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.Post

interface PostRepository {
    suspend fun getExploreFeed(page: Int, pageSize: Int): Result<List<FeedItem>>
    suspend fun lovePost(postId: String): Result<Unit>
    suspend fun unlovePost(postId: String): Result<Unit>
    suspend fun loveReply(replyId: String): Result<Unit>
    suspend fun unloveReply(replyId: String): Result<Unit>
    suspend fun getRandomPosts(count: Int): Result<List<Post>>
    suspend fun searchPosts(query: String, page: Int, pageSize: Int): Result<List<Post>>

    /**
     * Get posts near a specific location
     * @param location Center point for search
     * @param radiusKm Search radius in kilometers
     * @param limit Maximum number of posts to return
     * @return Result containing list of Posts with location data
     */
    suspend fun getNearbyPosts(location: Location, radiusKm: Double, limit: Int): Result<List<Post>>
}
