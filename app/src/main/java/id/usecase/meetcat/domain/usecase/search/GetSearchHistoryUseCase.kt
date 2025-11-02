package id.usecase.meetcat.domain.usecase.search

import id.usecase.meetcat.domain.repository.SearchHistoryRepository

class GetSearchHistoryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return searchHistoryRepository.getSearchHistory()
    }
}
