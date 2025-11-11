package id.usecase.meetcat.presentation.screen.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.presentation.component.card.PostCard
import id.usecase.meetcat.presentation.component.card.ReplyCard
import id.usecase.meetcat.presentation.component.chip.NewUpdatesChip
import id.usecase.meetcat.presentation.component.state.EmptyView
import id.usecase.meetcat.presentation.component.state.ErrorView
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
    isBottomNavVisible: Boolean = true
) {
    val feedItems = viewModel.feedItems.collectAsLazyPagingItems()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val hasNewUpdates by viewModel.hasNewUpdates.collectAsStateWithLifecycle()
    val newUpdatesCount by viewModel.newUpdatesCount.collectAsStateWithLifecycle()
    val shouldScrollToTop by viewModel.shouldScrollToTop.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lifecycle observer to start/stop polling
    // ON_START: Screen becomes visible (start polling)
    // ON_STOP: Screen becomes invisible (stop polling to save battery)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    viewModel.onEvent(ExploreUiEvent.ScreenStarted)
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.onEvent(ExploreUiEvent.ScreenStopped)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
        currentUserId = currentUserId,
        isBottomNavVisible = isBottomNavVisible,
        hasNewUpdates = hasNewUpdates,
        newUpdatesCount = newUpdatesCount,
        shouldScrollToTop = shouldScrollToTop
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
    currentUserId: String? = null,
    isBottomNavVisible: Boolean = true,
    hasNewUpdates: Boolean = false,
    newUpdatesCount: Int = 0,
    shouldScrollToTop: Boolean = false
) {
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
                FloatingActionButton(
                    onClick = onNavigateToCreatePost,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 80.dp) // Position above navbar
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Post"
                    )
                }
            }
        }
    ) { paddingValues ->
        val loadState = feedItems.loadState

        when {
            loadState.refresh is LoadState.Loading && feedItems.itemCount == 0 -> {
                LoadingView(modifier = Modifier.padding(paddingValues))
            }

            loadState.refresh is LoadState.Error && feedItems.itemCount == 0 -> {
                ErrorView(
                    message = (loadState.refresh as LoadState.Error).error.message
                        ?: "Failed to load feed",
                    onRetry = { feedItems.refresh() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            loadState.refresh is LoadState.NotLoading && feedItems.itemCount == 0 -> {
                EmptyView(
                    message = "No posts yet\nBe the first to share a cat photo!",
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    PullToRefreshBox(
                        isRefreshing = loadState.refresh is LoadState.Loading && feedItems.itemCount > 0,
                        onRefresh = {
                            feedItems.refresh()
                            onEvent(ExploreUiEvent.Refresh)
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FeedList(
                            feedItems = feedItems,
                            viewModel = viewModel,
                            onEvent = onEvent,
                            onShowBottomNav = onShowBottomNav,
                            onHideBottomNav = onHideBottomNav,
                            onNavigateToReply = onNavigateToReply,
                            currentUserId = currentUserId,
                            shouldScrollToTop = shouldScrollToTop
                        )
                    }

                    // New Updates Chip
                    NewUpdatesChip(
                        visible = hasNewUpdates,
                        count = newUpdatesCount,
                        onClick = {
                            onEvent(ExploreUiEvent.NewUpdatesChipClicked)
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
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
    currentUserId: String? = null,
    shouldScrollToTop: Boolean = false
) {
    // Use scroll position from ViewModel to preserve across navigation
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.scrollIndex,
        initialFirstVisibleItemScrollOffset = viewModel.scrollOffset
    )

    // Handle smooth scroll to top when chip is clicked
    LaunchedEffect(shouldScrollToTop) {
        if (shouldScrollToTop) {
            lazyListState.animateScrollToItem(0)
            onEvent(ExploreUiEvent.ScrolledToTop)
            viewModel.resetScrollToTop()
            // Also refresh to load new items
            feedItems.refresh()
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
            // Clear new updates chip when user manually scrolls to top
            if (currentIndex == 0 && currentOffset == 0) {
                onEvent(ExploreUiEvent.ScrolledToTop)
            }

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
            } else if (isScrollingDown && currentIndex > 0) {
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
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
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
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
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

// Note: Previews for ExploreContent are not included as they require LazyPagingItems
// which cannot be easily mocked in Compose previews.
