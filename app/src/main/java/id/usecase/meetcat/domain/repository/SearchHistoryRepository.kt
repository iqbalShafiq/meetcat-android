package id.usecase.meetcat.domain.repository

interface SearchHistoryRepository {
    suspend fun getSearchHistory(): Result<List<String>>
    suspend fun saveSearchQuery(query: String): Result<Unit>
    suspend fun deleteSearchQuery(query: String): Result<Unit>
    suspend fun clearAllSearchHistory(): Result<Unit>
}
