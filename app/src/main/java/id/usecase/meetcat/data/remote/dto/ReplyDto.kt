package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.Reply
import kotlinx.serialization.Serializable

@Serializable
data class ReplyDto(
    val id: String,
    val originalPostId: String,
    val originalPost: PostDto,
    val userId: String,
    val user: UserDto,
    val text: String,
    val mediaItems: List<MediaItemDto>? = null,
    val location: LocationDto? = null,
    val lovesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLoved: Boolean = false,
    val createdAt: Long
)

fun ReplyDto.toDomain(): Reply {
    return Reply(
        id = id,
        originalPostId = originalPostId,
        originalPost = originalPost.toDomain(),
        userId = userId,
        user = user.toDomain(),
        text = text,
        mediaItems = mediaItems?.map { it.toDomain() },
        location = location?.toDomain(),
        lovesCount = lovesCount,
        commentsCount = commentsCount,
        isLoved = isLoved,
        createdAt = createdAt
    )
}
