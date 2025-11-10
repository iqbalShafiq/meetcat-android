package id.usecase.meetcat.data.repository

import id.usecase.meetcat.data.remote.datasource.PostRemoteDataSource
import id.usecase.meetcat.data.remote.dto.CreateCommentRequest
import id.usecase.meetcat.data.remote.dto.toDomain
import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.Reply
import id.usecase.meetcat.domain.repository.PostRepository

class PostRepositoryImpl(
    private val remoteDataSource: PostRemoteDataSource
) : PostRepository {

    override suspend fun getExploreFeed(page: Int, pageSize: Int): Result<List<FeedItem>> {
        return try {
            val response = remoteDataSource.getExploreFeed(page, pageSize)

            if (response.success && response.data != null) {
                val feedItems = response.data.data.map { it.toDomain() }
                Result.success(feedItems)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch explore feed")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun lovePost(postId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.lovePost(postId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to love post")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlovePost(postId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.unlovePost(postId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to unlove post")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loveReply(replyId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.loveReply(replyId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to love reply")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unloveReply(replyId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.unloveReply(replyId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to unlove reply")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRandomPosts(count: Int): Result<List<Post>> {
        return try {
            val response = remoteDataSource.getRandomPosts(count)

            if (response.success && response.data != null) {
                val posts = response.data.map { it.toDomain() }
                Result.success(posts)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch random posts")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPosts(query: String, page: Int, pageSize: Int): Result<List<Post>> {
        return try {
            val response = remoteDataSource.searchPosts(query, page, pageSize)

            if (response.success && response.data != null) {
                val posts = response.data.data.map { it.toDomain() }
                Result.success(posts)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to search posts")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNearbyPosts(location: Location, radiusKm: Double, limit: Int): Result<List<Post>> {
        return try {
            val response = remoteDataSource.getNearbyPosts(
                latitude = location.latitude,
                longitude = location.longitude,
                radiusKm = radiusKm,
                limit = limit
            )

            if (response.success && response.data != null) {
                val posts = response.data.map { it.toDomain() }
                Result.success(posts)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch nearby posts")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val response = remoteDataSource.getPostById(postId)

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch post")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReplyById(replyId: String): Result<Reply> {
        return try {
            val response = remoteDataSource.getReplyById(replyId)

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch reply")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostComments(postId: String): Result<List<Comment>> {
        return try {
            val response = remoteDataSource.getPostComments(postId)

            if (response.success && response.data != null) {
                val comments = response.data.map { it.toDomain() }
                Result.success(comments)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch comments")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReplyComments(replyId: String): Result<List<Comment>> {
        return try {
            val response = remoteDataSource.getReplyComments(replyId)

            if (response.success && response.data != null) {
                val comments = response.data.map { it.toDomain() }
                Result.success(comments)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch comments")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loveComment(commentId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.loveComment(commentId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to love comment")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unloveComment(commentId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.unloveComment(commentId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to unlove comment")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPost(
        caption: String,
        mediaUris: List<android.net.Uri>?,
        location: Location?
    ): Result<Post> {
        return try {
            val response = remoteDataSource.createPost(
                caption = caption,
                mediaUris = mediaUris,
                location = location
            )

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to create post")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(
        postId: String,
        caption: String?,
        mediaUris: List<android.net.Uri>?,
        location: Location?,
        keepExistingMedia: Boolean
    ): Result<Post> {
        return try {
            val response = remoteDataSource.updatePost(
                postId = postId,
                caption = caption,
                mediaUris = mediaUris,
                location = location,
                keepExistingMedia = keepExistingMedia
            )

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to update post")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReply(
        originalPostId: String,
        text: String,
        mediaUris: List<android.net.Uri>?,
        location: Location?
    ): Result<Reply> {
        return try {
            val response = remoteDataSource.createReply(
                originalPostId = originalPostId,
                text = text,
                mediaUris = mediaUris,
                location = location
            )

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to create reply")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReply(
        replyId: String,
        text: String?,
        mediaUris: List<android.net.Uri>?,
        location: Location?,
        keepExistingMedia: Boolean
    ): Result<Reply> {
        return try {
            val response = remoteDataSource.updateReply(
                replyId = replyId,
                text = text,
                mediaUris = mediaUris,
                location = location,
                keepExistingMedia = keepExistingMedia
            )

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to update reply")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createComment(
        postId: String?,
        replyId: String?,
        text: String
    ): Result<Comment> {
        return try {
            val request = CreateCommentRequest(
                postId = postId,
                replyId = replyId,
                text = text
            )

            val response = remoteDataSource.createComment(request)

            if (response.success && response.data != null) {
                Result.success(response.data.toDomain())
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to create comment")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
