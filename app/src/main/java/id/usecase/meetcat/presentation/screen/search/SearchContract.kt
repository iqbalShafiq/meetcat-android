package id.usecase.meetcat.presentation.screen.search

import androidx.compose.runtime.Immutable
import id.usecase.meetcat.domain.model.Post
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class SearchUiState(
    val query: String = "",
    val isSearchActive: Boolean = false,
    val searchHistory: ImmutableList<String> = persistentListOf(),
    val randomPosts: ImmutableList<Post> = persistentListOf(),
    val searchResults: ImmutableList<Post> = persistentListOf(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null
)

sealed class SearchUiEvent {
    data class QueryChanged(val query: String) : SearchUiEvent()
    data object SearchActivated : SearchUiEvent()
    data object SearchDeactivated : SearchUiEvent()
    data object SubmitSearch : SearchUiEvent()
    data class SelectSearchHistory(val query: String) : SearchUiEvent()
    data class DeleteSearchHistory(val query: String) : SearchUiEvent()
    data object ClearAllSearchHistory : SearchUiEvent()
    data object LoadRandomPosts : SearchUiEvent()
    data class NavigateToPost(val postId: String) : SearchUiEvent()
    data class NavigateToProfile(val userId: String) : SearchUiEvent()
}

sealed class SearchUiEffect {
    data class NavigateToPost(val postId: String) : SearchUiEffect()
    data class NavigateToProfile(val userId: String) : SearchUiEffect()
    data class ShowError(val message: String) : SearchUiEffect()
}
