package id.usecase.meetcat.presentation.screen.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.presentation.component.card.PostCard
import id.usecase.meetcat.presentation.component.card.ReplyCard
import id.usecase.meetcat.presentation.component.state.EmptyView
import id.usecase.meetcat.presentation.component.state.ErrorView
import id.usecase.meetcat.presentation.component.state.LoadingView
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToEditPost: (String) -> Unit = {},
    onNavigateToVideoEditor: () -> Unit = {},
    isBottomNavVisible: Boolean = true
) {
    val feedItems = viewModel.feedItems.collectAsLazyPagingItems()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val showNewPostsChip by viewModel.showNewPostsChip.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update latest timestamp when feed items change
    LaunchedEffect(feedItems.itemCount) {
        if (feedItems.itemCount > 0) {
            feedItems.peek(0)?.let { firstItem ->
                val timestamp = when (firstItem) {
                    is FeedItem.PostItem -> firstItem.post.createdAt
                    is FeedItem.ReplyItem -> firstItem.reply.createdAt
                }
                viewModel.updateLatestTimestamp(timestamp)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ExploreUiEffect.NavigateToPost -> {
                    onNavigateToPost(effect.postId)
                }

                is ExploreUiEffect.NavigateToProfile -> {
                    onNavigateToProfile(effect.userId)
                }

                is ExploreUiEffect.NavigateToComments -> {
                    onNavigateToPost(effect.postId)
                }

                is ExploreUiEffect.NavigateToReply -> {
                    onNavigateToPost(effect.postId)
                }

                is ExploreUiEffect.NavigateToEditPost -> {
                    onNavigateToEditPost(effect.postId)
                }

                is ExploreUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ExploreContent(
        feedItems = feedItems,
        viewModel = viewModel,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav,
        onNavigateToReply = onNavigateToReply,
        onNavigateToCreatePost = onNavigateToCreatePost,
        onNavigateToVideoEditor = onNavigateToVideoEditor,
        currentUserId = currentUserId,
        isBottomNavVisible = isBottomNavVisible,
        showNewPostsChip = showNewPostsChip
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreContent(
    feedItems: LazyPagingItems<FeedItem>,
    viewModel: ExploreViewModel,
    onEvent: (ExploreUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
    onNavigateToReply: (String) -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToVideoEditor: () -> Unit = {},
    currentUserId: String? = null,
    isBottomNavVisible: Boolean = true,
    showNewPostsChip: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isBottomNavVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = scaleOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(bottom = 52.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Video Editor FAB
                    FloatingActionButton(
                        onClick = onNavigateToVideoEditor,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Video Editor"
                        )
                    }

                    // Create Post FAB
                    FloatingActionButton(
                        onClick = onNavigateToCreatePost,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Post"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            val loadState = feedItems.loadState

            when (loadState.refresh) {
                is LoadState.Loading if feedItems.itemCount == 0 -> {
                    LoadingView(modifier = Modifier.padding(paddingValues))
                }

                is LoadState.Error if feedItems.itemCount == 0 -> {
                    ErrorView(
                        message = (loadState.refresh as LoadState.Error).error.message
                            ?: "Failed to load feed",
                        onRetry = { feedItems.refresh() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                is LoadState.NotLoading if feedItems.itemCount == 0 -> {
                    EmptyView(
                        message = "No posts yet\nBe the first to share a cat photo!",
                        modifier = Modifier.padding(paddingValues)
                    )
                }

                else -> {
                    PullToRefreshBox(
                        isRefreshing = loadState.refresh is LoadState.Loading && feedItems.itemCount > 0,
                        onRefresh = { feedItems.refresh() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        FeedList(
                            feedItems = feedItems,
                            viewModel = viewModel,
                            onEvent = onEvent,
                            onShowBottomNav = onShowBottomNav,
                            onHideBottomNav = onHideBottomNav,
                            onNavigateToReply = onNavigateToReply,
                            currentUserId = currentUserId
                        )
                    }
                }
            }

            // New Posts Chip - overlay at the top
            AnimatedVisibility(
                visible = showNewPostsChip,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = paddingValues.calculateTopPadding() + 8.dp)
            ) {
                NewPostsChip(
                    onClick = {
                        coroutineScope.launch {
                            // Trigger smooth scroll and refresh
                            onEvent(ExploreUiEvent.NewPostsChipClick)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NewPostsChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text("New posts available")
        },
        icon = {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier
    )
}

@Composable
private fun FeedList(
    feedItems: LazyPagingItems<FeedItem>,
    onEvent: (ExploreUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
    onNavigateToReply: (String) -> Unit = {},
    viewModel: ExploreViewModel,
    currentUserId: String? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Use scroll position from ViewModel to preserve across navigation
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.scrollIndex,
        initialFirstVisibleItemScrollOffset = viewModel.scrollOffset
    )

    // Listen for chip click to scroll to top
    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.showNewPostsChip.value }
            .collect { showChip ->
                if (!showChip && lazyListState.firstVisibleItemIndex > 0) {
                    // Chip was just dismissed, scroll to top smoothly
                    lazyListState.animateScrollToItem(0)
                    // Refresh feed after scroll
                    feedItems.refresh()
                }
            }
    }

    // Track previous scroll position to detect scroll direction
    var previousIndex by remember { mutableIntStateOf(lazyListState.firstVisibleItemIndex) }
    var previousOffset by remember { mutableIntStateOf(lazyListState.firstVisibleItemScrollOffset) }

    // Save scroll position to ViewModel
    LaunchedEffect(Unit) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            viewModel.scrollIndex = index
            viewModel.scrollOffset = offset
        }
    }

    // Detect scroll direction and show/hide navbar accordingly
    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
                lazyListState.isScrollInProgress
            )
        }.collect { (currentIndex, currentOffset, isScrolling) ->
            // Only detect scroll direction when user is actively scrolling
            if (!isScrolling) return@collect

            // Determine scroll direction
            val isScrollingDown = if (currentIndex != previousIndex) {
                currentIndex > previousIndex
            } else {
                currentOffset > previousOffset
            }

            // Show navbar when scrolling up or at the top, hide when scrolling down
            if (currentIndex == 0 && currentOffset < 100) {
                // Always show at the very top
                onShowBottomNav()
            } else if (!isScrollingDown) {
                // Scrolling up - show navbar
                onShowBottomNav()
            } else if (currentIndex > 0) {
                // Scrolling down and not at top - hide navbar
                onHideBottomNav()
            }

            // Update previous position
            previousIndex = currentIndex
            previousOffset = currentOffset
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 88.dp // 80dp navbar + 8dp spacing
        ),
        state = lazyListState
    ) {
        items(
            count = feedItems.itemCount,
            key = { index ->
                feedItems[index]?.let { item ->
                    when (item) {
                        is FeedItem.PostItem -> "post_${item.post.id}"
                        is FeedItem.ReplyItem -> "reply_${item.reply.id}"
                    }
                } ?: "item_$index"
            },
            contentType = { index ->
                feedItems[index]?.let { item ->
                    when (item) {
                        is FeedItem.PostItem -> "post"
                        is FeedItem.ReplyItem -> "reply"
                    }
                } ?: "unknown"
            }
        ) { index ->
            feedItems[index]?.let { item ->
                when (item) {
                    is FeedItem.PostItem -> {
                        PostCard(
                            post = item.post,
                            onPostClick = {
                                onEvent(ExploreUiEvent.NavigateToPost(item.post.id))
                            },
                            onProfileClick = {
                                onEvent(ExploreUiEvent.NavigateToProfile(item.post.userId))
                            },
                            onLoveClick = {
                                onEvent(ExploreUiEvent.LovePost(item.post.id))
                            },
                            onCommentClick = {
                                onEvent(ExploreUiEvent.NavigateToComments(item.post.id))
                            },
                            onReplyClick = {
                                onEvent(ExploreUiEvent.NavigateToReply(item.post.id))
                            },
                            onShareClick = {
                                // Share action
                            },
                            currentUserId = currentUserId,
                            onEditClick = {
                                onEvent(ExploreUiEvent.NavigateToEditPost(item.post.id))
                            }
                        )
                    }

                    is FeedItem.ReplyItem -> {
                        ReplyCard(
                            reply = item.reply,
                            onReplyClick = {
                                onNavigateToReply(item.reply.id)
                            },
                            onProfileClick = {
                                onEvent(ExploreUiEvent.NavigateToProfile(item.reply.userId))
                            },
                            onOriginalPostClick = {
                                onEvent(ExploreUiEvent.NavigateToPost(item.reply.originalPostId))
                            },
                            onOriginalProfileClick = {
                                onEvent(ExploreUiEvent.NavigateToProfile(item.reply.originalPost.userId))
                            },
                            onLoveClick = {
                                onEvent(ExploreUiEvent.LoveReply(item.reply.id))
                            },
                            onCommentClick = {
                                onEvent(ExploreUiEvent.NavigateToComments(item.reply.id))
                            },
                            onShareClick = {
                                // Share action
                            }
                        )
                    }
                }
            }
        }

        // Loading indicator at bottom (append state)
        feedItems.loadState.append.let { appendState ->
            when (appendState) {
                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
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
                            TextButton(onClick = { feedItems.retry() }) {
                                Text(
                                    text = "Failed to load more. Tap to retry",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                is LoadState.NotLoading -> {
                    if (appendState.endOfPaginationReached && feedItems.itemCount > 0) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "You've reached the end",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}