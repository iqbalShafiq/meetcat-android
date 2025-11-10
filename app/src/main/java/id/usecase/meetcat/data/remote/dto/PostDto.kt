package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.Post
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val id: String,
    val userId: String,
    val user: UserDto,
    val caption: String,
    val mediaItems: List<MediaItemDto> = emptyList(),
    val location: LocationDto? = null,
    val lovesCount: Int = 0,
    val commentsCount: Int = 0,
    val repliesCount: Int = 0,
    val isLoved: Boolean = false,
    val createdAt: Long
)

fun PostDto.toDomain(): Post {
    return Post(
        id = id,
        userId = userId,
        user = user.toDomain(),
        caption = caption,
        mediaItems = mediaItems.map { it.toDomain() },
        location = location?.toDomain(),
        lovesCount = lovesCount,
        commentsCount = commentsCount,
        repliesCount = repliesCount,
        isLoved = isLoved,
        createdAt = createdAt
    )
}
