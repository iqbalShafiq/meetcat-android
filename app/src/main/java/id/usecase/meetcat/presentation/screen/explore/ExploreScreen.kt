package id.usecase.meetcat.presentation.screen.explore

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ExploreUiEffect.NavigateToPost -> {
                }

                is ExploreUiEffect.NavigateToProfile -> {
                }

                is ExploreUiEffect.NavigateToComments -> {
                }

                is ExploreUiEffect.NavigateToReply -> {
                }

                is ExploreUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ExploreContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreContent(
    uiState: ExploreUiState,
    onEvent: (ExploreUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
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
        when {
            uiState.isLoading && uiState.feedItems.isEmpty() -> {
                LoadingView(modifier = Modifier.padding(paddingValues))
            }

            uiState.error != null && uiState.feedItems.isEmpty() -> {
                ErrorView(
                    message = uiState.error,
                    onRetry = { onEvent(ExploreUiEvent.Refresh) },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            uiState.feedItems.isEmpty() -> {
                EmptyView(
                    message = "No posts yet\nBe the first to share a cat photo!",
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(ExploreUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    FeedList(
                        feedItems = uiState.feedItems,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedList(
    feedItems: List<FeedItem>,
    onEvent: (ExploreUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = feedItems,
            key = { item ->
                when (item) {
                    is FeedItem.PostItem -> "post_${item.post.id}"
                    is FeedItem.ReplyItem -> "reply_${item.reply.id}"
                }
            },
            contentType = { item ->
                when (item) {
                    is FeedItem.PostItem -> "post"
                    is FeedItem.ReplyItem -> "reply"
                }
            }
        ) { item ->
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
                            onEvent(ExploreUiEvent.NavigateToPost(item.reply.id))
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
}


@Preview(showBackground = true)
@Composable
private fun ExploreScreenLoadingPreview() {
    MeetCatTheme {
        ExploreContent(
            uiState = ExploreUiState(isLoading = true),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenErrorPreview() {
    MeetCatTheme {
        ExploreContent(
            uiState = ExploreUiState(
                isLoading = false,
                error = "Failed to load feed. Please try again."
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenEmptyPreview() {
    MeetCatTheme {
        ExploreContent(
            uiState = ExploreUiState(
                isLoading = false,
                feedItems = persistentListOf()
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExploreScreenSuccessPreview() {
    MeetCatTheme {
        ExploreContent(
            uiState = ExploreUiState(
                isLoading = false,
                feedItems = listOf(
                    FeedItem.PostItem(PreviewData.mockPost),
                    FeedItem.ReplyItem(PreviewData.mockReply),
                    FeedItem.PostItem(PreviewData.mockPostWithVideo),
                    FeedItem.ReplyItem(PreviewData.mockReplyTextOnly)
                ).toImmutableList()
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
