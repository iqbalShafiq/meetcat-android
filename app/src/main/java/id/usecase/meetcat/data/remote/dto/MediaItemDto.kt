package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.MediaItem
import kotlinx.serialization.Serializable

@Serializable
data class MediaItemDto(
    val type: String, // "image" or "video"
    val url: String,
    val thumbnailUrl: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val duration: Long? = null // Only for videos
)

fun MediaItemDto.toDomain(): MediaItem {
    return when (type.lowercase()) {
        "image" -> MediaItem.Image(
            url = url,
            thumbnailUrl = thumbnailUrl,
            width = width,
            height = height
        )
        "video" -> MediaItem.Video(
            url = url,
            thumbnailUrl = thumbnailUrl,
            duration = duration ?: 0,
            width = width,
            height = height
        )
        else -> MediaItem.Image(
            url = url,
            thumbnailUrl = thumbnailUrl,
            width = width,
            height = height
        )
    }
}
