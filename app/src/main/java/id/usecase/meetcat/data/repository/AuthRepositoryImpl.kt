package id.usecase.meetcat.data.repository

import android.util.Log
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
            Log.d(TAG, "Attempting login for email: $email")
            val response = remoteDataSource.login(email, password)

            if (response.success && response.data != null) {
                val authUser = response.data.toDomain()
                Log.d(TAG, "Login successful, saving token")
                // Save token
                tokenStorage.saveToken(authUser.token)
                Log.d(TAG, "Token saved, user: ${authUser.user.username}")
                Result.success(authUser)
            } else {
                Log.e(TAG, "Login failed: ${response.error?.message}")
                Result.failure(
                    Exception(response.error?.message ?: "Login failed")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login exception: ${e.message}", e)
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
            Log.d(TAG, "Attempting register for email: $email, username: $username")
            val response = remoteDataSource.register(email, username, displayName, password)

            if (response.success && response.data != null) {
                val authUser = response.data.toDomain()
                Log.d(TAG, "Register successful, saving token")
                // Save token
                tokenStorage.saveToken(authUser.token)
                Log.d(TAG, "Token saved, user: ${authUser.user.username}")
                Result.success(authUser)
            } else {
                Log.e(TAG, "Register failed: ${response.error?.message}")
                Result.failure(
                    Exception(response.error?.message ?: "Registration failed")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Register exception: ${e.message}", e)
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
            Log.d(TAG, "Getting current user")
            if (!tokenStorage.hasToken()) {
                Log.d(TAG, "No token found, returning null")
                return null
            }

            Log.d(TAG, "Token found, calling API")
            val response = remoteDataSource.getCurrentUser()

            if (response.success && response.data != null) {
                val token = tokenStorage.getToken() ?: ""
                Log.d(TAG, "Current user retrieved: ${response.data.username}")
                response.data.copy(token = token).toDomain()
            } else {
                Log.e(TAG, "Get current user failed: ${response.error?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get current user exception: ${e.message}", e)
            null
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "Logging out")
            val response = remoteDataSource.logout()

            // Clear token regardless of API response
            tokenStorage.clearToken()
            Log.d(TAG, "Token cleared after logout")

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
            Log.e(TAG, "Logout exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
