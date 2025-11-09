package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.FeedItem
import kotlinx.serialization.Serializable

@Serializable
data class FeedItemDto(
    val type: String, // "post" or "reply"
    val post: PostDto? = null,
    val reply: ReplyDto? = null
)

fun FeedItemDto.toDomain(): FeedItem {
    return when (type.lowercase()) {
        "post" -> FeedItem.PostItem(post!!.toDomain())
        "reply" -> FeedItem.ReplyItem(reply!!.toDomain())
        else -> throw IllegalArgumentException("Unknown feed item type: $type")
    }
}
