package id.usecase.meetcat.domain.model

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val user: User,
    val text: String,
    val lovesCount: Int,
    val isLoved: Boolean,
    val createdAt: Long
)
