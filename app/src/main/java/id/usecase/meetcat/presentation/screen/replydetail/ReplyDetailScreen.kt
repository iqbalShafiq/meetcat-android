package id.usecase.meetcat.presentation.screen.replydetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import id.usecase.meetcat.presentation.component.button.LoveButton
import id.usecase.meetcat.presentation.component.card.CommentCard
import id.usecase.meetcat.presentation.component.card.ReplyCard
import id.usecase.meetcat.presentation.component.dialog.CommentDialog
import id.usecase.meetcat.presentation.component.state.ErrorView
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.presentation.util.shareReply
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReplyDetailScreen(
    replyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToPost: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReplyDetailViewModel = koinViewModel(key = "replyDetail_$replyId") { parametersOf(replyId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showCommentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ReplyDetailUiEffect.NavigateBack -> onNavigateBack()
                is ReplyDetailUiEffect.NavigateToProfile -> onNavigateToProfile(effect.userId)
                is ReplyDetailUiEffect.NavigateToOriginalPost -> onNavigateToPost(effect.postId)
                is ReplyDetailUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ReplyDetailUiEffect.ShowCommentDialog -> {
                    showCommentDialog = true
                }
                is ReplyDetailUiEffect.ShowShareDialog -> {
                    uiState.reply?.let { shareReply(context, it) }
                }
                is ReplyDetailUiEffect.CommentSubmitted -> {
                    showCommentDialog = false
                    snackbarHostState.showSnackbar("Comment submitted successfully")
                }
            }
        }
    }

    ReplyDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )

    // Comment Dialog
    if (showCommentDialog && uiState.reply != null) {
        CommentDialog(
            post = null,
            reply = uiState.reply,
            onDismiss = { showCommentDialog = false },
            onCommentSubmit = { commentText ->
                viewModel.onEvent(ReplyDetailUiEvent.SubmitComment(commentText))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReplyDetailContent(
    uiState: ReplyDetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (ReplyDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom
    )

    var expanded by rememberSaveable { mutableStateOf(true) }
    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reply Detail") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.reply != null) {
                HorizontalFloatingToolbar(
                    expanded = expanded,
                    scrollBehavior = scrollBehavior,
                    colors = vibrantColors,
                    modifier = Modifier
                        .offset(y = -FloatingToolbarDefaults.ScreenOffset)
                        .zIndex(1f),
                    content = {
                        // Back button
                        IconButton(
                            onClick = { onEvent(ReplyDetailUiEvent.NavigateBack) },
                            modifier = Modifier.focusProperties { canFocus = expanded }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }

                        // Love button
                        LoveButton(
                            isLoved = uiState.reply.isLoved,
                            onClick = { onEvent(ReplyDetailUiEvent.LoveReply) },
                            modifier = Modifier.focusProperties { canFocus = expanded }
                        )

                        // Comment button
                        IconButton(
                            onClick = { onEvent(ReplyDetailUiEvent.CommentClick) },
                            modifier = Modifier.focusProperties { canFocus = expanded }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ModeComment,
                                contentDescription = "Comments"
                            )
                        }

                        // Share button
                        IconButton(
                            onClick = { onEvent(ReplyDetailUiEvent.ShareClick) },
                            modifier = Modifier.focusProperties { canFocus = expanded }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share"
                            )
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = modifier
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.reply == null -> {
                LoadingView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null && uiState.reply == null -> {
                ErrorView(
                    message = uiState.error,
                    onRetry = { onEvent(ReplyDetailUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.reply != null -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(ReplyDetailUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .floatingToolbarVerticalNestedScroll(
                                expanded = expanded,
                                onExpand = { expanded = true },
                                onCollapse = { expanded = false }
                            ),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 104.dp)
                    ) {
                        // Reply Item
                        item {
                            ReplyCard(
                                reply = uiState.reply,
                                onReplyClick = { /* Already in detail */ },
                                onProfileClick = {
                                    onEvent(ReplyDetailUiEvent.NavigateToProfile(uiState.reply.userId))
                                },
                                onOriginalPostClick = {
                                    onEvent(ReplyDetailUiEvent.NavigateToOriginalPost(uiState.reply.originalPostId))
                                },
                                onOriginalProfileClick = {
                                    onEvent(ReplyDetailUiEvent.NavigateToProfile(uiState.reply.originalPost.userId))
                                },
                                onLoveClick = { onEvent(ReplyDetailUiEvent.LoveReply) },
                                onCommentClick = { onEvent(ReplyDetailUiEvent.CommentClick) },
                                onShareClick = { onEvent(ReplyDetailUiEvent.ShareClick) },
                                modifier = Modifier.padding(horizontal = 8.dp),
                                showActions = false
                            )
                        }

                        // Comments Section Header
                        if (uiState.comments.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(top = 12.dp, bottom = 8.dp)
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
                                Column {
                                    CommentCard(
                                        comment = comment,
                                        onCommentClick = {
                                            // Note: Comment detail/expansion not implemented in current scope
                                            // Comments are display-only. Future: could expand to show nested replies
                                        },
                                        onProfileClick = {
                                            onEvent(ReplyDetailUiEvent.NavigateToProfile(comment.userId))
                                        },
                                        onLoveClick = {
                                            onEvent(ReplyDetailUiEvent.LoveComment(comment.id))
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )

                                    if (comment.id != uiState.comments.last().id) HorizontalDivider()
                                }
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
private fun ReplyDetailScreenPreview() {
    MeetCatTheme {
        ReplyDetailContent(
            uiState = ReplyDetailUiState(
                reply = PreviewData.mockReply,
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
private fun ReplyDetailScreenNoCommentsPreview() {
    MeetCatTheme {
        ReplyDetailContent(
            uiState = ReplyDetailUiState(
                reply = PreviewData.mockReply,
                comments = persistentListOf(),
                isLoading = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
