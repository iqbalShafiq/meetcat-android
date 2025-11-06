package id.usecase.meetcat.domain.repository

import id.usecase.meetcat.domain.model.AuthUser

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(
        email: String,
        username: String,
        displayName: String,
        password: String
    ): Result<AuthUser>
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun isLoggedIn(): Boolean
    suspend fun getCurrentUser(): AuthUser?
    suspend fun logout(): Result<Unit>
}
