package id.usecase.meetcat.data.remote.dto

import id.usecase.meetcat.domain.model.AuthUser
import kotlinx.serialization.Serializable

@Serializable
data class AuthUserDto(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val token: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val displayName: String,
    val password: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class IsLoggedInResponse(
    val isLoggedIn: Boolean
)

fun AuthUserDto.toDomain(): AuthUser {
    return AuthUser(
        id = id,
        email = email,
        username = username,
        displayName = displayName,
        profileImageUrl = profileImageUrl,
        token = token
    )
}
