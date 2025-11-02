package id.usecase.meetcat.domain.model

data class Post(
    val id: String,
    val userId: String,
    val user: User,
    val caption: String,
    val mediaItems: List<MediaItem>,
    val location: Location?,
    val lovesCount: Int,
    val commentsCount: Int,
    val repliesCount: Int,
    val isLoved: Boolean,
    val createdAt: Long
)
