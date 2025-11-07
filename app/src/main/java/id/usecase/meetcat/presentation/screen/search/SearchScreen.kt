package id.usecase.meetcat.presentation.screen.search

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.presentation.component.state.EmptyView
import id.usecase.meetcat.presentation.component.state.ErrorView
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.screen.search.component.SearchHistoryItem
import id.usecase.meetcat.presentation.screen.search.component.SearchPostGridItem
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onNavigateToPost: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SearchUiEffect.NavigateToPost -> onNavigateToPost(effect.postId)
                is SearchUiEffect.NavigateToProfile -> onNavigateToProfile(effect.userId)
                is SearchUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    SearchContent(
        uiState = uiState,
        searchResults = searchResults,
        viewModel = viewModel,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    uiState: SearchUiState,
    searchResults: LazyPagingItems<Post>,
    viewModel: SearchViewModel,
    onEvent: (SearchUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.query,
                        onQueryChange = { onEvent(SearchUiEvent.QueryChanged(it)) },
                        onSearch = { onEvent(SearchUiEvent.SubmitSearch) },
                        expanded = uiState.isSearchActive,
                        onExpandedChange = { expanded ->
                            if (expanded) {
                                onEvent(SearchUiEvent.SearchActivated)
                            } else {
                                onEvent(SearchUiEvent.SearchDeactivated)
                            }
                        },
                        placeholder = { Text("Search posts...") },
                        leadingIcon = {
                            if (uiState.isSearchActive) {
                                IconButton(onClick = {
                                    onEvent(SearchUiEvent.SearchDeactivated)
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = {
                                    onEvent(SearchUiEvent.QueryChanged(""))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = uiState.isSearchActive,
                onExpandedChange = { expanded ->
                    if (expanded) {
                        onEvent(SearchUiEvent.SearchActivated)
                    } else {
                        onEvent(SearchUiEvent.SearchDeactivated)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (uiState.isSearchActive) 0.dp else 16.dp)
                    .padding(top = 8.dp, bottom = 8.dp)
            ) {
                // Search history content
                if (uiState.searchHistory.isNotEmpty()) {
                    Column {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            items(
                                items = uiState.searchHistory,
                                key = { it }
                            ) { query ->
                                SearchHistoryItem(
                                    query = query,
                                    onQueryClick = {
                                        onEvent(SearchUiEvent.SelectSearchHistory(query))
                                    },
                                    onDeleteClick = {
                                        onEvent(SearchUiEvent.DeleteSearchHistory(query))
                                    }
                                )
                            }
                        }

                        HorizontalDivider()

                        TextButton(
                            onClick = { onEvent(SearchUiEvent.ClearAllSearchHistory) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("Clear all search history")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No search history",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Main content - Random grid or search results
            when {
                uiState.isLoading -> {
                    LoadingView()
                }
                uiState.error != null && uiState.randomPosts.isEmpty() -> {
                    ErrorView(
                        message = uiState.error,
                        onRetry = { onEvent(SearchUiEvent.LoadRandomPosts) }
                    )
                }
                // Show search results if we have paginated data
                searchResults.itemCount > 0 || searchResults.loadState.refresh is LoadState.Loading -> {
                    SearchResultsGrid(
                        searchResults = searchResults,
                        viewModel = viewModel,
                        onPostClick = { onEvent(SearchUiEvent.NavigateToPost(it)) },
                        onShowBottomNav = onShowBottomNav,
                        onHideBottomNav = onHideBottomNav
                    )
                }
                // Show empty state if search was performed but no results
                searchResults.loadState.refresh is LoadState.NotLoading && searchResults.itemCount == 0 && uiState.query.isNotEmpty() -> {
                    EmptyView(message = "No results found")
                }
                else -> {
                    RandomPostsGrid(
                        posts = uiState.randomPosts,
                        viewModel = viewModel,
                        onPostClick = { onEvent(SearchUiEvent.NavigateToPost(it)) },
                        onShowBottomNav = onShowBottomNav,
                        onHideBottomNav = onHideBottomNav
                    )
                }
            }
        }
    }
}

@Composable
private fun RandomPostsGrid(
    posts: ImmutableList<Post>,
    viewModel: SearchViewModel,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    // Use scroll position from ViewModel to preserve across navigation
    val lazyGridState = rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = viewModel.randomPostsScrollIndex
    )
    var previousIndex by remember { mutableIntStateOf(0) }

    // Save scroll position to ViewModel
    LaunchedEffect(Unit) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.randomPostsScrollIndex = index
            }
    }

    // Scroll detection: show when scrolling up, hide when scrolling down
    LaunchedEffect(Unit) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { currentIndex: Int ->
                if (currentIndex < previousIndex) {
                    // Scrolling up
                    onShowBottomNav()
                } else if (currentIndex > previousIndex) {
                    // Scrolling down
                    onHideBottomNav()
                }
                previousIndex = currentIndex
            }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalItemSpacing = 0.dp,
        state = lazyGridState
    ) {
        items(
            items = posts,
            key = { it.id }
        ) { post ->
            SearchPostGridItem(
                post = post,
                onClick = { onPostClick(post.id) }
            )
        }
    }
}

@Composable
private fun SearchResultsGrid(
    searchResults: LazyPagingItems<Post>,
    viewModel: SearchViewModel,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    // Use scroll position from ViewModel to preserve across navigation
    val lazyGridState = rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = viewModel.searchResultsScrollIndex
    )
    var previousIndex by remember { mutableIntStateOf(0) }

    // Save scroll position to ViewModel
    LaunchedEffect(Unit) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.searchResultsScrollIndex = index
            }
    }

    // Scroll detection: show when scrolling up, hide when scrolling down
    LaunchedEffect(Unit) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { currentIndex: Int ->
                if (currentIndex < previousIndex) {
                    // Scrolling up
                    onShowBottomNav()
                } else if (currentIndex > previousIndex) {
                    // Scrolling down
                    onHideBottomNav()
                }
                previousIndex = currentIndex
            }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalItemSpacing = 0.dp,
        state = lazyGridState
    ) {
        // Display paged items
        items(
            count = searchResults.itemCount,
            key = { index -> searchResults.peek(index)?.id ?: index }
        ) { index ->
            searchResults[index]?.let { post ->
                SearchPostGridItem(
                    post = post,
                    onClick = { onPostClick(post.id) }
                )
            }
        }

        // Handle loading state at bottom for infinite scroll
        searchResults.loadState.append.let { appendState ->
            when (appendState) {
                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Button(onClick = { searchResults.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    // End of list - do nothing
                }
            }
        }

        // Handle initial loading state
        searchResults.loadState.refresh.let { refreshState ->
            when (refreshState) {
                is LoadState.Loading -> {
                    if (searchResults.itemCount == 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                        }
                    }
                }
                is LoadState.Error -> {
                    if (searchResults.itemCount == 0) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Error loading results")
                                androidx.compose.material3.Button(onClick = { searchResults.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

// Note: Previews for SearchContent are not included as they require LazyPagingItems
// which cannot be easily mocked in Compose previews.
