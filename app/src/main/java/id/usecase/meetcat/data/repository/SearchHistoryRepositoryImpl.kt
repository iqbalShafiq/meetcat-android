package id.usecase.meetcat.data.repository

import id.usecase.meetcat.data.remote.datasource.SearchHistoryRemoteDataSource
import id.usecase.meetcat.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val remoteDataSource: SearchHistoryRemoteDataSource
) : SearchHistoryRepository {

    override suspend fun getSearchHistory(): Result<List<String>> {
        return try {
            val response = remoteDataSource.getSearchHistory()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to fetch search history")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSearchQuery(query: String): Result<Unit> {
        return try {
            val response = remoteDataSource.saveSearchQuery(query)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to save search query")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSearchQuery(query: String): Result<Unit> {
        return try {
            val response = remoteDataSource.deleteSearchQuery(query)

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to delete search query")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllSearchHistory(): Result<Unit> {
        return try {
            val response = remoteDataSource.clearAllSearchHistory()

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(response.error?.message ?: "Failed to clear search history")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
