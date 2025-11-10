package id.usecase.meetcat.domain.repository

import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply

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

    /**
     * Get a post by its ID
     * @param postId The ID of the post
     * @return Result containing the Post
     */
    suspend fun getPostById(postId: String): Result<Post>

    /**
     * Get a reply by its ID
     * @param replyId The ID of the reply
     * @return Result containing the Reply
     */
    suspend fun getReplyById(replyId: String): Result<Reply>

    /**
     * Get comments for a post
     * @param postId The ID of the post
     * @return Result containing list of Comments
     */
    suspend fun getPostComments(postId: String): Result<List<Comment>>

    /**
     * Get comments for a reply
     * @param replyId The ID of the reply
     * @return Result containing list of Comments
     */
    suspend fun getReplyComments(replyId: String): Result<List<Comment>>

    /**
     * Love a comment
     * @param commentId The ID of the comment
     * @return Result<Unit>
     */
    suspend fun loveComment(commentId: String): Result<Unit>

    /**
     * Unlove a comment
     * @param commentId The ID of the comment
     * @return Result<Unit>
     */
    suspend fun unloveComment(commentId: String): Result<Unit>

    /**
     * Create a new post
     * @param caption Post caption
     * @param mediaUris Optional list of media file URIs
     * @param location Optional location
     * @return Result containing the created Post
     */
    suspend fun createPost(
        caption: String,
        mediaUris: List<android.net.Uri>? = null,
        location: Location? = null
    ): Result<Post>

    /**
     * Update an existing post
     * @param postId ID of the post to update
     * @param caption Optional updated caption
     * @param mediaUris Optional list of media file URIs (replaces all existing media if provided)
     * @param location Optional location (null to keep existing, empty Location to remove)
     * @param keepExistingMedia If true and mediaUris is null, keeps existing media
     * @return Result containing the updated Post
     */
    suspend fun updatePost(
        postId: String,
        caption: String? = null,
        mediaUris: List<android.net.Uri>? = null,
        location: Location? = null,
        keepExistingMedia: Boolean = false
    ): Result<Post>

    /**
     * Create a reply to a post
     * @param originalPostId ID of the original post
     * @param text Reply text
     * @param mediaUris Optional list of media file URIs
     * @param location Optional location
     * @return Result containing the created Reply
     */
    suspend fun createReply(
        originalPostId: String,
        text: String,
        mediaUris: List<android.net.Uri>? = null,
        location: Location? = null
    ): Result<Reply>

    /**
     * Update an existing reply
     * @param replyId ID of the reply to update
     * @param text Optional updated text
     * @param mediaUris Optional list of media file URIs (replaces all existing media if provided)
     * @param location Optional location (null to keep existing, empty Location to remove)
     * @param keepExistingMedia If true and mediaUris is null, keeps existing media
     * @return Result containing the updated Reply
     */
    suspend fun updateReply(
        replyId: String,
        text: String? = null,
        mediaUris: List<android.net.Uri>? = null,
        location: Location? = null,
        keepExistingMedia: Boolean = false
    ): Result<Reply>

    /**
     * Create a comment on a post or reply
     * @param postId Optional post ID (if commenting on a post)
     * @param replyId Optional reply ID (if commenting on a reply)
     * @param text Comment text
     * @return Result containing the created Comment
     */
    suspend fun createComment(
        postId: String? = null,
        replyId: String? = null,
        text: String
    ): Result<Comment>
}
