package id.usecase.meetcat.data.repository

import id.usecase.meetcat.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.delay

/**
 * Fake implementation of SearchHistoryRepository for testing and development.
 * Uses in-memory storage for search history.
 *
 * Features:
 * - No Android dependencies (fully unit testable)
 * - In-memory search history storage
 * - Test isolation via reset() method
 * - Maintains last 10 searches
 */
class FakeSearchHistoryRepository : SearchHistoryRepository {

    private val searchHistory = mutableListOf<String>()

    override suspend fun getSearchHistory(): Result<List<String>> {
        delay(100)
        return Result.success(searchHistory.toList())
    }

    override suspend fun saveSearchQuery(query: String): Result<Unit> {
        delay(100)

        // Remove if already exists (to move to top)
        searchHistory.remove(query)

        // Add to the beginning
        searchHistory.add(0, query)

        // Keep only last 10 items
        if (searchHistory.size > 10) {
            searchHistory.removeAt(searchHistory.size - 1)
        }

        return Result.success(Unit)
    }

    override suspend fun deleteSearchQuery(query: String): Result<Unit> {
        delay(100)
        searchHistory.remove(query)
        return Result.success(Unit)
    }

    override suspend fun clearAllSearchHistory(): Result<Unit> {
        delay(100)
        searchHistory.clear()
        return Result.success(Unit)
    }

    /**
     * Reset repository state for test isolation.
     * Call this between test cases to ensure clean state.
     */
    fun reset() {
        searchHistory.clear()
    }
}
