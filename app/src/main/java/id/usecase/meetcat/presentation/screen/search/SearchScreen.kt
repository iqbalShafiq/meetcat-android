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
    val snackbarHostState = remember { SnackbarHostState() }

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
                uiState.query.isNotEmpty() && uiState.searchResults.isEmpty() && !uiState.isSearching -> {
                    EmptyView(message = "No results found for \"${uiState.query}\"")
                }
                uiState.searchResults.isNotEmpty() -> {
                    SearchResultsGrid(
                        posts = uiState.searchResults,
                        onPostClick = { onEvent(SearchUiEvent.NavigateToPost(it)) },
                        onShowBottomNav = onShowBottomNav,
                        onHideBottomNav = onHideBottomNav
                    )
                }
                else -> {
                    RandomPostsGrid(
                        posts = uiState.randomPosts,
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
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    val lazyGridState = rememberLazyStaggeredGridState()
    var previousIndex by remember { mutableIntStateOf(0) }

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
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 4.dp,
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
    posts: ImmutableList<Post>,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    val lazyGridState = rememberLazyStaggeredGridState()
    var previousIndex by remember { mutableIntStateOf(0) }

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
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalItemSpacing = 4.dp,
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

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    MeetCatTheme {
        SearchContent(
            uiState = SearchUiState(
                randomPosts = persistentListOf(
                    Post(
                        id = "1",
                        userId = "user1",
                        user = User(
                            id = "user1",
                            username = "catuser",
                            displayName = "Cat User",
                            bio = null,
                            profileImageUrl = null,
                            followersCount = 0,
                            followingCount = 0,
                            postsCount = 0,
                            isFollowing = false,
                            createdAt = System.currentTimeMillis()
                        ),
                        caption = "Cute cat",
                        mediaItems = listOf(
                            MediaItem.Image(
                                url = "https://example.com/cat.jpg",
                                thumbnailUrl = null,
                                width = 800,
                                height = 600
                            )
                        ),
                        location = null,
                        lovesCount = 10,
                        commentsCount = 5,
                        repliesCount = 2,
                        isLoved = false,
                        createdAt = System.currentTimeMillis()
                    )
                )
            ),
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchScreenActivePreview() {
    MeetCatTheme {
        SearchContent(
            uiState = SearchUiState(
                isSearchActive = true,
                query = "persian cat",
                searchHistory = persistentListOf(
                    "cute cat",
                    "orange tabby",
                    "persian cat"
                )
            ),
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
