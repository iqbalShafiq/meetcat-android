package id.usecase.meetcat.data.repository

import id.usecase.meetcat.data.local.TokenStorage
import id.usecase.meetcat.data.remote.datasource.AuthRemoteDataSource
import id.usecase.meetcat.data.remote.dto.toDomain
import id.usecase.meetcat.domain.model.AuthUser
import id.usecase.meetcat.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return try {
            val response = remoteDataSource.login(email, password)

            if (response.success && response.data != null) {
                val authUser = response.data.toDomain()
                // Save token
                tokenStorage.saveToken(authUser.token)
                Result.success(authUser)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Login failed")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        displayName: String,
        password: String
    ): Result<AuthUser> {
        return try {
            val response = remoteDataSource.register(email, username, displayName, password)

            if (response.success && response.data != null) {
                val authUser = response.data.toDomain()
                // Save token
                tokenStorage.saveToken(authUser.token)
                Result.success(authUser)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Registration failed")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            val response = remoteDataSource.resetPassword(email)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Reset password failed")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenStorage.hasToken()
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return try {
            if (!tokenStorage.hasToken()) {
                return null
            }

            val response = remoteDataSource.getCurrentUser()

            if (response.success && response.data != null) {
                val token = tokenStorage.getToken() ?: ""
                response.data.copy(token = token).toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val response = remoteDataSource.logout()

            // Clear token regardless of API response
            tokenStorage.clearToken()

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Logout failed")
                )
            }
        } catch (e: Exception) {
            // Still clear token even if API call fails
            tokenStorage.clearToken()
            Result.failure(e)
        }
    }
}
