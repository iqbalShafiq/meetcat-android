package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.Comment
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    val id: String,
    val postId: String,
    val userId: String,
    val user: UserDto,
    val text: String,
    val lovesCount: Int = 0,
    val isLoved: Boolean = false,
    val createdAt: Long
)

fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        postId = postId,
        userId = userId,
        user = user.toDomain(),
        text = text,
        lovesCount = lovesCount,
        isLoved = isLoved,
        createdAt = createdAt
    )
}
