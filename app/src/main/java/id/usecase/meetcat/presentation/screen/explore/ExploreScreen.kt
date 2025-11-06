package id.usecase.meetcat.presentation.screen.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onNavigateToProfile: (String) -> Unit = {}
) {
    val feedItems = viewModel.feedItems.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

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

                is ExploreUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ExploreContent(
        feedItems = feedItems,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav,
        onNavigateToReply = onNavigateToReply
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreContent(
    feedItems: LazyPagingItems<FeedItem>,
    onEvent: (ExploreUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
    onNavigateToReply: (String) -> Unit = {}
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
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                PullToRefreshBox(
                    isRefreshing = loadState.refresh is LoadState.Loading && feedItems.itemCount > 0,
                    onRefresh = { feedItems.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    FeedList(
                        feedItems = feedItems,
                        onEvent = onEvent,
                        onShowBottomNav = onShowBottomNav,
                        onHideBottomNav = onHideBottomNav,
                        onNavigateToReply = onNavigateToReply
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
    onNavigateToReply: (String) -> Unit = {}
) {
    val lazyListState = rememberLazyListState()

    // Simple scroll detection: hide when scrolling down, show when at top
    LaunchedEffect(lazyListState.isScrollInProgress) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { index ->
                if (index == 0) {
                    onShowBottomNav()
                } else {
                    onHideBottomNav()
                }
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
