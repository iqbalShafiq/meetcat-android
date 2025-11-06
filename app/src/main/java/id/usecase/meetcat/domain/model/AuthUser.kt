package id.usecase.meetcat.domain.model

data class AuthUser(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val profileImageUrl: String?,
    val token: String
)
