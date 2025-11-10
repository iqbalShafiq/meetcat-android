package id.usecase.meetcat.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * CreateCommentRequest is still used for JSON-based comment creation
 * (comments don't support media uploads, so multipart is not needed)
 */
@Serializable
data class CreateCommentRequest(
    val postId: String? = null,
    val replyId: String? = null,
    val text: String
)
