package id.usecase.meetcat.domain.usecase.search

import id.usecase.meetcat.domain.repository.SearchHistoryRepository

class ClearSearchHistoryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return searchHistoryRepository.clearAllSearchHistory()
    }
}
