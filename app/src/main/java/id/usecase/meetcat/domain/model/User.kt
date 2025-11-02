package id.usecase.meetcat.domain.model

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String?,
    val profileImageUrl: String?,
    val followersCount: Int,
    val followingCount: Int,
    val postsCount: Int,
    val isFollowing: Boolean,
    val createdAt: Long
)
