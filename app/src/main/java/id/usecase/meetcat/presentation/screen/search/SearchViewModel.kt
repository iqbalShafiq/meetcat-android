package id.usecase.meetcat.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.paging.SearchPagingSource
import id.usecase.meetcat.domain.usecase.post.GetRandomPostsUseCase
import id.usecase.meetcat.domain.usecase.post.SearchPostsUseCase
import id.usecase.meetcat.domain.usecase.search.ClearSearchHistoryUseCase
import id.usecase.meetcat.domain.usecase.search.DeleteSearchQueryUseCase
import id.usecase.meetcat.domain.usecase.search.GetSearchHistoryUseCase
import id.usecase.meetcat.domain.usecase.search.SaveSearchQueryUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val getRandomPostsUseCase: GetRandomPostsUseCase,
    private val searchPostsUseCase: SearchPostsUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    private val saveSearchQueryUseCase: SaveSearchQueryUseCase,
    private val deleteSearchQueryUseCase: DeleteSearchQueryUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SearchUiEffect>()
    val uiEffect: Flow<SearchUiEffect> = _uiEffect.receiveAsFlow()

    // Preserve scroll positions across navigation
    var randomPostsScrollIndex: Int = 0
    var searchResultsScrollIndex: Int = 0

    // Track the submitted query for Paging3
    private val _searchQuery = MutableStateFlow<String?>(null)

    // Paging3 Flow for search results - recreates when query changes
    val searchResults: Flow<PagingData<Post>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isNullOrBlank()) {
                // No search query, return empty paging data
                flowOf(PagingData.empty())
            } else {
                // Create new Pager for this query
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        prefetchDistance = PREFETCH_DISTANCE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { SearchPagingSource(query, searchPostsUseCase) }
                ).flow
            }
        }
        .cachedIn(viewModelScope)

    init {
        // Load random posts immediately when ViewModel is created
        loadRandomPosts()
        loadSearchHistory()
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> updateQuery(event.query)
            is SearchUiEvent.SearchActivated -> activateSearch()
            is SearchUiEvent.SearchDeactivated -> deactivateSearch()
            is SearchUiEvent.SubmitSearch -> submitSearch()
            is SearchUiEvent.SelectSearchHistory -> selectSearchHistory(event.query)
            is SearchUiEvent.DeleteSearchHistory -> deleteSearchHistory(event.query)
            is SearchUiEvent.ClearAllSearchHistory -> clearAllSearchHistory()
            is SearchUiEvent.LoadRandomPosts -> loadRandomPosts()
            is SearchUiEvent.NavigateToPost -> {
                viewModelScope.launch {
                    _uiEffect.send(SearchUiEffect.NavigateToPost(event.postId))
                }
            }
            is SearchUiEvent.NavigateToProfile -> {
                viewModelScope.launch {
                    _uiEffect.send(SearchUiEffect.NavigateToProfile(event.userId))
                }
            }
        }
    }

    private fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun activateSearch() {
        _uiState.update { it.copy(isSearchActive = true) }
        loadSearchHistory()
    }

    private fun deactivateSearch() {
        _uiState.update {
            it.copy(
                isSearchActive = false,
                query = ""
            )
        }
        // Clear search results when deactivating
        _searchQuery.value = null
    }

    private fun submitSearch() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSearchActive = false,
                    isSearching = true
                )
            }

            // Save query to history
            saveSearchQueryUseCase(query)

            // Trigger search via Paging3 by updating the query
            _searchQuery.value = query

            // Mark searching as complete after a short delay
            // (Paging3 will handle the actual loading state)
            _uiState.update { it.copy(isSearching = false) }
        }
    }

    private fun selectSearchHistory(query: String) {
        _uiState.update { it.copy(query = query) }
        submitSearch()
    }

    private fun deleteSearchHistory(query: String) {
        viewModelScope.launch {
            deleteSearchQueryUseCase(query)
            loadSearchHistory()
        }
    }

    private fun clearAllSearchHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
            loadSearchHistory()
        }
    }

    private fun loadRandomPosts() {
        // Allow reload even if we have posts
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getRandomPostsUseCase()

            result.fold(
                onSuccess = { posts ->
                    _uiState.update {
                        it.copy(
                            randomPosts = posts.toImmutableList(),
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                    _uiEffect.send(
                        SearchUiEffect.ShowError(
                            error.message ?: "Failed to load posts"
                        )
                    )
                }
            )
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            val result = getSearchHistoryUseCase()

            result.fold(
                onSuccess = { history ->
                    _uiState.update {
                        it.copy(searchHistory = history.toImmutableList())
                    }
                },
                onFailure = {
                    // Silently fail - not critical
                }
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
        private const val PREFETCH_DISTANCE = 10
    }
}
