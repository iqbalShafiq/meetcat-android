package id.usecase.meetcat.domain.usecase.search

import id.usecase.meetcat.domain.repository.SearchHistoryRepository

class DeleteSearchQueryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(query: String): Result<Unit> {
        return searchHistoryRepository.deleteSearchQuery(query)
    }
}
