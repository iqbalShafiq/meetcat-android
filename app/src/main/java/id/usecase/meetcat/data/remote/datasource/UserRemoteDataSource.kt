package id.usecase.meetcat.data.remote.datasource

import id.usecase.meetcat.data.remote.dto.ApiResponse
import id.usecase.meetcat.data.remote.dto.FeedItemDto
import id.usecase.meetcat.data.remote.dto.PaginatedResponse
import id.usecase.meetcat.data.remote.dto.PostDto
import id.usecase.meetcat.data.remote.dto.ReplyDto
import id.usecase.meetcat.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class UserRemoteDataSource(
    private val httpClient: HttpClient
) {

    suspend fun getUserPosts(userId: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<PostDto>> {
        return httpClient.get("/v1/users/$userId/posts") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getUserReplies(userId: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<ReplyDto>> {
        return httpClient.get("/v1/users/$userId/replies") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getUserLovedItems(userId: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<FeedItemDto>> {
        return httpClient.get("/v1/users/$userId/loved-items") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getFollowers(userId: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<UserDto>> {
        return httpClient.get("/v1/users/$userId/followers") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getFollowing(userId: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<UserDto>> {
        return httpClient.get("/v1/users/$userId/following") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun followUser(userId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/users/$userId/follow").body()
    }

    suspend fun unfollowUser(userId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/users/$userId/unfollow").body()
    }
}
