package id.usecase.meetcat.data.remote.datasource

import id.usecase.meetcat.data.remote.dto.ApiResponse
import id.usecase.meetcat.data.remote.dto.CommentDto
import id.usecase.meetcat.data.remote.dto.FeedItemDto
import id.usecase.meetcat.data.remote.dto.PaginatedResponse
import id.usecase.meetcat.data.remote.dto.PostDto
import id.usecase.meetcat.data.remote.dto.ReplyDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class PostRemoteDataSource(
    private val httpClient: HttpClient
) {

    suspend fun getExploreFeed(page: Int, pageSize: Int): ApiResponse<PaginatedResponse<FeedItemDto>> {
        return httpClient.get("/posts/explore") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun lovePost(postId: String): ApiResponse<Unit> {
        return httpClient.post("/posts/$postId/love").body()
    }

    suspend fun unlovePost(postId: String): ApiResponse<Unit> {
        return httpClient.post("/posts/$postId/unlove").body()
    }

    suspend fun loveReply(replyId: String): ApiResponse<Unit> {
        return httpClient.post("/replies/$replyId/love").body()
    }

    suspend fun unloveReply(replyId: String): ApiResponse<Unit> {
        return httpClient.post("/replies/$replyId/unlove").body()
    }

    suspend fun getRandomPosts(count: Int): ApiResponse<List<PostDto>> {
        return httpClient.get("/posts/random") {
            parameter("count", count)
        }.body()
    }

    suspend fun searchPosts(query: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<PostDto>> {
        return httpClient.get("/posts/search") {
            parameter("query", query)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getNearbyPosts(
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
        limit: Int
    ): ApiResponse<List<PostDto>> {
        return httpClient.get("/posts/nearby") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("radiusKm", radiusKm)
            parameter("limit", limit)
        }.body()
    }

    suspend fun getPostById(postId: String): ApiResponse<PostDto> {
        return httpClient.get("/posts/$postId").body()
    }

    suspend fun getReplyById(replyId: String): ApiResponse<ReplyDto> {
        return httpClient.get("/replies/$replyId").body()
    }

    suspend fun getPostComments(postId: String): ApiResponse<List<CommentDto>> {
        return httpClient.get("/posts/$postId/comments").body()
    }

    suspend fun getReplyComments(replyId: String): ApiResponse<List<CommentDto>> {
        return httpClient.get("/replies/$replyId/comments").body()
    }

    suspend fun loveComment(commentId: String): ApiResponse<Unit> {
        return httpClient.post("/comments/$commentId/love").body()
    }

    suspend fun unloveComment(commentId: String): ApiResponse<Unit> {
        return httpClient.post("/comments/$commentId/unlove").body()
    }
}
