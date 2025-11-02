package id.usecase.meetcat.domain.model

data class Reply(
    val id: String,
    val originalPostId: String,
    val originalPost: Post,
    val userId: String,
    val user: User,
    val text: String,
    val mediaItems: List<MediaItem>?,
    val location: Location?,
    val lovesCount: Int,
    val commentsCount: Int,
    val isLoved: Boolean,
    val createdAt: Long
)
