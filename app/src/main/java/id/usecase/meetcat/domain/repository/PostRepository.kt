package id.usecase.meetcat.domain.repository

import id.usecase.meetcat.domain.model.FeedItem

interface PostRepository {
    suspend fun getExploreFeed(page: Int, pageSize: Int): Result<List<FeedItem>>
    suspend fun lovePost(postId: String): Result<Unit>
    suspend fun unlovePost(postId: String): Result<Unit>
    suspend fun loveReply(replyId: String): Result<Unit>
    suspend fun unloveReply(replyId: String): Result<Unit>
}
