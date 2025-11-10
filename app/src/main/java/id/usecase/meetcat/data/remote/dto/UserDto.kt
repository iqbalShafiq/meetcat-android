package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val displayName: String,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isFollowing: Boolean = false,
    val createdAt: Long = 0
)

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        displayName = displayName,
        bio = bio,
        profileImageUrl = profileImageUrl,
        followersCount = followersCount,
        followingCount = followingCount,
        postsCount = postsCount,
        isFollowing = isFollowing,
        createdAt = createdAt
    )
}
