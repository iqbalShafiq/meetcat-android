package id.usecase.meetcat.data.repository

import id.usecase.meetcat.domain.model.AuthUser
import id.usecase.meetcat.domain.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * Fake implementation of AuthRepository for testing and development.
 * Uses in-memory storage instead of SharedPreferences to enable pure unit testing.
 *
 * Features:
 * - No Android dependencies (fully unit testable)
 * - In-memory user storage
 * - Test isolation via reset() method
 * - Thread-safe operations
 */
class FakeAuthRepository : AuthRepository {

    // In-memory storage (replaces SharedPreferences)
    private var currentUser: AuthUser? = null
    private var isUserLoggedIn: Boolean = false

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

            // Save to in-memory storage (replaces SharedPreferences)
            currentUser = authUser
            isUserLoggedIn = true

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
        return isUserLoggedIn
    }

    override suspend fun getCurrentUser(): AuthUser? {
        // For development bypass login: always return a mock current user
        // even when not logged in to prevent "not found" errors
        return currentUser ?: AuthUser(
            id = "dev_user_123",
            email = "dev@meetcat.com",
            username = "devuser",
            displayName = "Dev User",
            profileImageUrl = "https://picsum.photos/200?random=999",
            token = "dev_token"
        )
    }

    override suspend fun logout(): Result<Unit> {
        // Clear in-memory storage (replaces SharedPreferences.clear())
        currentUser = null
        isUserLoggedIn = false
        return Result.success(Unit)
    }

    /**
     * Reset repository state for test isolation.
     * Call this between test cases to ensure clean state.
     */
    fun reset() {
        currentUser = null
        isUserLoggedIn = false
        // Reset to default mock users
        mockUsers.clear()
        mockUsers["test@meetcat.com"] = MockUser(
            email = "test@meetcat.com",
            username = "testuser",
            displayName = "Test User",
            password = "password123"
        )
    }

    private data class MockUser(
        val email: String,
        val username: String,
        val displayName: String,
        val password: String
    )
}
