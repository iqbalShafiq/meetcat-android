package id.usecase.meetcat.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val caption: String,
    val mediaItems: List<MediaItemDto>? = null,
    val location: LocationDto? = null
)

@Serializable
data class CreateReplyRequest(
    val originalPostId: String,
    val text: String,
    val mediaItems: List<MediaItemDto>? = null,
    val location: LocationDto? = null
)

@Serializable
data class CreateCommentRequest(
    val postId: String? = null,
    val replyId: String? = null,
    val text: String
)
