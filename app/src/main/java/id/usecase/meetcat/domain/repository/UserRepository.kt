package id.usecase.meetcat.domain.repository

import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User

/**
 * Repository interface for user-related operations.
 * Handles user profiles, posts, followers, following, etc.
 */
interface UserRepository {

    /**
     * Get posts created by a user (paginated)
     * @param userId User ID
     * @param page Page number (0-based)
     * @param pageSize Number of items per page
     * @return Result containing list of Posts
     */
    suspend fun getUserPosts(userId: String, page: Int, pageSize: Int): Result<List<Post>>

    /**
     * Get replies created by a user (paginated)
     * @param userId User ID
     * @param page Page number (0-based)
     * @param pageSize Number of items per page
     * @return Result containing list of Reply items
     */
    suspend fun getUserReplies(userId: String, page: Int, pageSize: Int): Result<List<FeedItem.ReplyItem>>

    /**
     * Get items (posts + replies) loved by a user (paginated)
     * @param userId User ID
     * @param page Page number (0-based)
     * @param pageSize Number of items per page
     * @return Result containing list of FeedItems (mixed posts and replies)
     */
    suspend fun getUserLovedItems(userId: String, page: Int, pageSize: Int): Result<List<FeedItem>>

    /**
     * Get followers of a user (paginated)
     * @param userId User ID
     * @param page Page number (0-based)
     * @param pageSize Number of items per page
     * @return Result containing list of Users
     */
    suspend fun getFollowers(userId: String, page: Int, pageSize: Int): Result<List<User>>

    /**
     * Get users that a user is following (paginated)
     * @param userId User ID
     * @param page Page number (0-based)
     * @param pageSize Number of items per page
     * @return Result containing list of Users
     */
    suspend fun getFollowing(userId: String, page: Int, pageSize: Int): Result<List<User>>

    /**
     * Follow a user
     * @param userId User ID to follow
     * @return Result<Unit>
     */
    suspend fun followUser(userId: String): Result<Unit>

    /**
     * Unfollow a user
     * @param userId User ID to unfollow
     * @return Result<Unit>
     */
    suspend fun unfollowUser(userId: String): Result<Unit>
}
