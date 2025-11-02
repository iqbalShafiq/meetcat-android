package id.usecase.meetcat.domain.usecase.search

import id.usecase.meetcat.domain.repository.SearchHistoryRepository

class SaveSearchQueryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(query: String): Result<Unit> {
        if (query.isBlank()) {
            return Result.success(Unit)
        }
        return searchHistoryRepository.saveSearchQuery(query)
    }
}
