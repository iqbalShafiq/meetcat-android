package id.usecase.meetcat.data.repository

import id.usecase.meetcat.data.remote.datasource.UserRemoteDataSource
import id.usecase.meetcat.data.remote.dto.toDomain
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.domain.repository.UserRepository

class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    override suspend fun getUserPosts(userId: String, page: Int, pageSize: Int): Result<List<Post>> {
        return try {
            val response = remoteDataSource.getUserPosts(userId, page, pageSize)

            if (response.success && response.data != null) {
                val posts = response.data.data.map { it.toDomain() }
                Result.success(posts)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch user posts")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserReplies(userId: String, page: Int, pageSize: Int): Result<List<FeedItem.ReplyItem>> {
        return try {
            val response = remoteDataSource.getUserReplies(userId, page, pageSize)

            if (response.success && response.data != null) {
                val replies = response.data.data.map { FeedItem.ReplyItem(it.toDomain()) }
                Result.success(replies)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch user replies")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserLovedItems(userId: String, page: Int, pageSize: Int): Result<List<FeedItem>> {
        return try {
            val response = remoteDataSource.getUserLovedItems(userId, page, pageSize)

            if (response.success && response.data != null) {
                val lovedItems = response.data.data.map { it.toDomain() }
                Result.success(lovedItems)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch loved items")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFollowers(userId: String, page: Int, pageSize: Int): Result<List<User>> {
        return try {
            val response = remoteDataSource.getFollowers(userId, page, pageSize)

            if (response.success && response.data != null) {
                val followers = response.data.data.map { it.toDomain() }
                Result.success(followers)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch followers")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFollowing(userId: String, page: Int, pageSize: Int): Result<List<User>> {
        return try {
            val response = remoteDataSource.getFollowing(userId, page, pageSize)

            if (response.success && response.data != null) {
                val following = response.data.data.map { it.toDomain() }
                Result.success(following)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch following")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun followUser(userId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.followUser(userId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to follow user")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> {
        return try {
            val response = remoteDataSource.unfollowUser(userId)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to unfollow user")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
