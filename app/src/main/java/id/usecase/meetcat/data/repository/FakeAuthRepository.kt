package id.usecase.meetcat.data.repository

import android.content.Context
import id.usecase.meetcat.domain.model.AuthUser
import id.usecase.meetcat.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class FakeAuthRepository(
    private val context: Context
) : AuthRepository {

    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Mock users database
    private val mockUsers = mutableMapOf(
        "test@meetcat.com" to MockUser(
            email = "test@meetcat.com",
            username = "testuser",
            displayName = "Test User",
            password = "password123"
        )
    )

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        delay(1000) // Simulate network delay

        val mockUser = mockUsers[email]

        return if (mockUser != null && mockUser.password == password) {
            val authUser = AuthUser(
                id = mockUser.email.hashCode().toString(),
                email = mockUser.email,
                username = mockUser.username,
                displayName = mockUser.displayName,
                profileImageUrl = null,
                token = "fake_token_${System.currentTimeMillis()}"
            )

            // Save to preferences
            prefs.edit().apply {
                putString("user_id", authUser.id)
                putString("email", authUser.email)
                putString("username", authUser.username)
                putString("display_name", authUser.displayName)
                putString("token", authUser.token)
                putBoolean("is_logged_in", true)
                apply()
            }

            Result.success(authUser)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    override suspend fun register(
        email: String,
        username: String,
        displayName: String,
        password: String
    ): Result<AuthUser> {
        delay(1000) // Simulate network delay

        // Check if user already exists
        if (mockUsers.containsKey(email)) {
            return Result.failure(Exception("Email already registered"))
        }

        // Check if username is taken
        if (mockUsers.values.any { it.username == username }) {
            return Result.failure(Exception("Username already taken"))
        }

        // Add new user to mock database
        mockUsers[email] = MockUser(email, username, displayName, password)

        // Auto login after register
        return login(email, password)
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        delay(1000) // Simulate network delay

        return if (mockUsers.containsKey(email)) {
            // In real app, send reset email
            Result.success(Unit)
        } else {
            Result.failure(Exception("Email not found"))
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    override suspend fun getCurrentUser(): AuthUser? {
        if (!isLoggedIn()) return null

        return AuthUser(
            id = prefs.getString("user_id", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            username = prefs.getString("username", "") ?: "",
            displayName = prefs.getString("display_name", "") ?: "",
            profileImageUrl = prefs.getString("profile_image_url", null),
            token = prefs.getString("token", "") ?: ""
        )
    }

    override suspend fun logout(): Result<Unit> {
        prefs.edit().clear().apply()
        return Result.success(Unit)
    }

    private data class MockUser(
        val email: String,
        val username: String,
        val displayName: String,
        val password: String
    )
}
