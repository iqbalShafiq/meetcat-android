package id.usecase.meetcat.data.remote.datasource

import id.usecase.meetcat.data.remote.dto.ApiResponse
import id.usecase.meetcat.data.remote.dto.SaveSearchQueryRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SearchHistoryRemoteDataSource(
    private val httpClient: HttpClient
) {

    suspend fun getSearchHistory(): ApiResponse<List<String>> {
        return httpClient.get("/v1/search-history").body()
    }

    suspend fun saveSearchQuery(query: String): ApiResponse<Unit> {
        return httpClient.post("/v1/search-history/save") {
            setBody(SaveSearchQueryRequest(query))
        }.body()
    }

    suspend fun deleteSearchQuery(query: String): ApiResponse<Unit> {
        return httpClient.delete("/v1/search-history/$query").body()
    }

    suspend fun clearAllSearchHistory(): ApiResponse<Unit> {
        return httpClient.delete("/v1/search-history/all").body()
    }
}
