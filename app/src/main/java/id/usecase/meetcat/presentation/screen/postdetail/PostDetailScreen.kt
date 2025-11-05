package id.usecase.meetcat.presentation.screen.postdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import id.usecase.meetcat.presentation.component.card.CommentCard
import id.usecase.meetcat.presentation.component.card.PostCard
import id.usecase.meetcat.presentation.component.dialog.CommentDialog
import id.usecase.meetcat.presentation.component.dialog.ReplyDialog
import id.usecase.meetcat.presentation.component.state.ErrorView
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.presentation.util.sharePost
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = koinViewModel { parametersOf(postId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showCommentDialog by remember { mutableStateOf(false) }
    var showReplyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is PostDetailUiEffect.NavigateBack -> onNavigateBack()
                is PostDetailUiEffect.NavigateToProfile -> onNavigateToProfile(effect.userId)
                is PostDetailUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is PostDetailUiEffect.ShowCommentDialog -> {
                    showCommentDialog = true
                }
                is PostDetailUiEffect.ShowReplyDialog -> {
                    showReplyDialog = true
                }
                is PostDetailUiEffect.ShowShareDialog -> {
                    uiState.post?.let { sharePost(context, it) }
                }
            }
        }
    }

    PostDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )

    // Comment Dialog
    if (showCommentDialog && uiState.post != null) {
        CommentDialog(
            post = uiState.post,
            reply = null,
            onDismiss = { showCommentDialog = false },
            onCommentSubmit = { commentText ->
                // TODO: Submit comment to repository
                snackbarHostState.currentSnackbarData?.dismiss()
                showCommentDialog = false
            }
        )
    }

    // Reply Dialog
    if (showReplyDialog && uiState.post != null) {
        ReplyDialog(
            post = uiState.post,
            onDismiss = { showReplyDialog = false },
            onReplySubmit = { replyText ->
                // TODO: Submit reply to repository
                snackbarHostState.currentSnackbarData?.dismiss()
                showReplyDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostDetailContent(
    uiState: PostDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (PostDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Detail") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PostDetailUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.post == null -> {
                LoadingView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null && uiState.post == null -> {
                ErrorView(
                    message = uiState.error,
                    onRetry = { onEvent(PostDetailUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.post != null -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(uiState.isRefreshing),
                    onRefresh = { onEvent(PostDetailUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Post Item
                        item {
                            PostCard(
                                post = uiState.post,
                                onPostClick = { /* Already in detail */ },
                                onProfileClick = {
                                    onEvent(PostDetailUiEvent.NavigateToProfile(uiState.post.userId))
                                },
                                onLoveClick = { onEvent(PostDetailUiEvent.LovePost) },
                                onCommentClick = { onEvent(PostDetailUiEvent.CommentClick) },
                                onReplyClick = { onEvent(PostDetailUiEvent.ReplyClick) },
                                onShareClick = { onEvent(PostDetailUiEvent.ShareClick) },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        // Comments Section Header
                        if (uiState.comments.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = "Comments (${uiState.comments.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Comments List
                            items(
                                items = uiState.comments,
                                key = { it.id }
                            ) { comment ->
                                CommentCard(
                                    comment = comment,
                                    onCommentClick = { /* TODO: Navigate to comment detail or expand */ },
                                    onProfileClick = {
                                        onEvent(PostDetailUiEvent.NavigateToProfile(comment.userId))
                                    },
                                    onLoveClick = {
                                        onEvent(PostDetailUiEvent.LoveComment(comment.id))
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            // No Comments Yet
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No comments yet",
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
}

@Preview(showBackground = true)
@Composable
private fun PostDetailScreenPreview() {
    MeetCatTheme {
        PostDetailContent(
            uiState = PostDetailUiState(
                post = PreviewData.mockPost,
                comments = listOf(
                    PreviewData.mockComment,
                    PreviewData.mockCommentLoved
                ).toImmutableList(),
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostDetailScreenNoCommentsPreview() {
    MeetCatTheme {
        PostDetailContent(
            uiState = PostDetailUiState(
                post = PreviewData.mockPost,
                comments = persistentListOf(),
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
