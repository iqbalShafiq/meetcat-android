package id.usecase.meetcat.data.remote.datasource

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import id.usecase.meetcat.data.remote.dto.ApiResponse
import id.usecase.meetcat.data.remote.dto.CommentDto
import id.usecase.meetcat.data.remote.dto.CreateCommentRequest
import id.usecase.meetcat.data.remote.dto.FeedItemDto
import id.usecase.meetcat.data.remote.dto.LocationDto
import id.usecase.meetcat.data.remote.dto.PaginatedResponse
import id.usecase.meetcat.data.remote.dto.PostDto
import id.usecase.meetcat.data.remote.dto.ReplyDto
import id.usecase.meetcat.data.remote.dto.toDto
import id.usecase.meetcat.domain.model.Location
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PostRemoteDataSource(
    private val httpClient: HttpClient,
    private val context: Context
) {

    suspend fun getExploreFeed(page: Int, pageSize: Int): ApiResponse<PaginatedResponse<FeedItemDto>> {
        return httpClient.get("/v1/posts/explore") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun lovePost(postId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/posts/$postId/love").body()
    }

    suspend fun unlovePost(postId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/posts/$postId/unlove").body()
    }

    suspend fun loveReply(replyId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/replies/$replyId/love").body()
    }

    suspend fun unloveReply(replyId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/replies/$replyId/unlove").body()
    }

    suspend fun getRandomPosts(count: Int): ApiResponse<List<PostDto>> {
        return httpClient.get("/v1/posts/random") {
            parameter("count", count)
        }.body()
    }

    suspend fun searchPosts(query: String, page: Int, pageSize: Int): ApiResponse<PaginatedResponse<PostDto>> {
        return httpClient.get("/v1/posts/search") {
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
        return httpClient.get("/v1/posts/nearby") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("radiusKm", radiusKm)
            parameter("limit", limit)
        }.body()
    }

    suspend fun getPostById(postId: String): ApiResponse<PostDto> {
        return httpClient.get("/v1/posts/$postId").body()
    }

    suspend fun getReplyById(replyId: String): ApiResponse<ReplyDto> {
        return httpClient.get("/v1/replies/$replyId").body()
    }

    suspend fun getPostComments(postId: String): ApiResponse<List<CommentDto>> {
        return httpClient.get("/v1/posts/$postId/comments").body()
    }

    suspend fun getReplyComments(replyId: String): ApiResponse<List<CommentDto>> {
        return httpClient.get("/v1/replies/$replyId/comments").body()
    }

    suspend fun loveComment(commentId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/comments/$commentId/love").body()
    }

    suspend fun unloveComment(commentId: String): ApiResponse<Unit> {
        return httpClient.post("/v1/comments/$commentId/unlove").body()
    }

    suspend fun createPost(
        caption: String,
        mediaUris: List<Uri>?,
        location: Location?
    ): ApiResponse<PostDto> {
        return httpClient.submitFormWithBinaryData(
            url = "/v1/posts",
            formData = formData {
                // Add caption
                append("caption", caption)

                // Add location as JSON string if provided
                location?.let {
                    val locationJson = Json.encodeToString(it.toDto())
                    append("location", locationJson)
                }

                // Add media files
                mediaUris?.forEachIndexed { index, uri ->
                    val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val fileName = getFileName(uri) ?: "media_$index"
                    val mimeType = getMimeType(uri, fileName)

                    // Validate file type
                    if (!isSupportedFileType(mimeType)) {
                        throw IllegalArgumentException(
                            "Invalid file type: $mimeType. Only JPEG, PNG, WebP images and MP4, MOV videos are allowed"
                        )
                    }

                    fileBytes?.let { bytes ->
                        append(
                            "media[]",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                }
            }
        ).body()
    }

    suspend fun updatePost(
        postId: String,
        caption: String?,
        mediaUris: List<Uri>?,
        location: Location?,
        keepExistingMedia: Boolean
    ): ApiResponse<PostDto> {
        return httpClient.submitFormWithBinaryData(
            url = "/v1/posts/$postId",
            formData = formData {
                // Add caption if provided
                caption?.let {
                    append("caption", it)
                }

                // Add location as JSON string if provided
                location?.let {
                    val locationJson = Json.encodeToString(it.toDto())
                    append("location", locationJson)
                }

                // Add keepExistingMedia flag
                append("keepExistingMedia", keepExistingMedia.toString())

                // Add media files if provided
                mediaUris?.forEachIndexed { index, uri ->
                    val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val fileName = getFileName(uri) ?: "media_$index"
                    val mimeType = getMimeType(uri, fileName)

                    // Validate file type
                    if (!isSupportedFileType(mimeType)) {
                        throw IllegalArgumentException(
                            "Invalid file type: $mimeType. Only JPEG, PNG, WebP images and MP4, MOV videos are allowed"
                        )
                    }

                    fileBytes?.let { bytes ->
                        append(
                            "media[]",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                }
            }
        ) {
            method = io.ktor.http.HttpMethod.Put
        }.body()
    }

    suspend fun createReply(
        originalPostId: String,
        text: String,
        mediaUris: List<Uri>?,
        location: Location?
    ): ApiResponse<ReplyDto> {
        return httpClient.submitFormWithBinaryData(
            url = "/v1/replies",
            formData = formData {
                // Add required fields
                append("originalPostId", originalPostId)
                append("text", text)

                // Add location as JSON string if provided
                location?.let {
                    val locationJson = Json.encodeToString(it.toDto())
                    append("location", locationJson)
                }

                // Add media files
                mediaUris?.forEachIndexed { index, uri ->
                    val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val fileName = getFileName(uri) ?: "media_$index"
                    val mimeType = getMimeType(uri, fileName)

                    // Validate file type
                    if (!isSupportedFileType(mimeType)) {
                        throw IllegalArgumentException(
                            "Invalid file type: $mimeType. Only JPEG, PNG, WebP images and MP4, MOV videos are allowed"
                        )
                    }

                    fileBytes?.let { bytes ->
                        append(
                            "media[]",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                }
            }
        ).body()
    }

    suspend fun updateReply(
        replyId: String,
        text: String?,
        mediaUris: List<Uri>?,
        location: Location?,
        keepExistingMedia: Boolean
    ): ApiResponse<ReplyDto> {
        return httpClient.submitFormWithBinaryData(
            url = "/v1/replies/$replyId",
            formData = formData {
                // Add text if provided
                text?.let {
                    append("text", it)
                }

                // Add location as JSON string if provided
                location?.let {
                    val locationJson = Json.encodeToString(it.toDto())
                    append("location", locationJson)
                }

                // Add keepExistingMedia flag
                append("keepExistingMedia", keepExistingMedia.toString())

                // Add media files if provided
                mediaUris?.forEachIndexed { index, uri ->
                    val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    val fileName = getFileName(uri) ?: "media_$index"
                    val mimeType = getMimeType(uri, fileName)

                    // Validate file type
                    if (!isSupportedFileType(mimeType)) {
                        throw IllegalArgumentException(
                            "Invalid file type: $mimeType. Only JPEG, PNG, WebP images and MP4, MOV videos are allowed"
                        )
                    }

                    fileBytes?.let { bytes ->
                        append(
                            "media[]",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, mimeType)
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                }
            }
        ) {
            method = io.ktor.http.HttpMethod.Put
        }.body()
    }

    suspend fun createComment(request: CreateCommentRequest): ApiResponse<CommentDto> {
        return httpClient.post("/v1/comments") {
            setBody(request)
        }.body()
    }

    /**
     * Get filename from URI
     */
    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    /**
     * Get MIME type from URI with fallback to extension-based detection
     */
    private fun getMimeType(uri: Uri, fileName: String?): String {
        // First, try to get MIME type from ContentResolver
        val mimeType = context.contentResolver.getType(uri)

        // If ContentResolver returns a valid MIME type, use it
        if (!mimeType.isNullOrBlank() && mimeType != "application/octet-stream") {
            return mimeType
        }

        // Fallback: determine MIME type from file extension
        val extension = fileName?.substringAfterLast('.', "")?.lowercase() ?: ""
        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            // Videos
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            // Default: return original or unknown
            else -> mimeType ?: "application/octet-stream"
        }
    }

    /**
     * Validate if file type is supported
     */
    private fun isSupportedFileType(mimeType: String): Boolean {
        return when {
            mimeType.startsWith("image/") -> {
                mimeType in listOf("image/jpeg", "image/png", "image/webp")
            }
            mimeType.startsWith("video/") -> {
                mimeType in listOf("video/mp4", "video/quicktime")
            }
            else -> false
        }
    }
}
