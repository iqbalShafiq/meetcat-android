package id.usecase.meetcat.presentation.screen.userprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.presentation.component.state.EmptyView
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.component.user.ProfileStat
import id.usecase.meetcat.presentation.component.user.UserProfileHeader
import id.usecase.meetcat.presentation.screen.profile.LovedList
import id.usecase.meetcat.presentation.screen.profile.PostsGrid
import id.usecase.meetcat.presentation.screen.profile.RepliesList
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPost: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = koinViewModel { parametersOf(userId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect Paging3 flows for each tab
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val replies = viewModel.replies.collectAsLazyPagingItems()
    val lovedItems = viewModel.lovedItems.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is UserProfileUiEffect.NavigateToPost -> {
                    onNavigateToPost(effect.postId)
                }
                is UserProfileUiEffect.NavigateBack -> {
                    onNavigateBack()
                }
                is UserProfileUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    UserProfileContent(
        uiState = uiState,
        posts = posts,
        replies = replies,
        lovedItems = lovedItems,
        viewModel = viewModel,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileContent(
    uiState: UserProfileUiState,
    posts: LazyPagingItems<Post>,
    replies: LazyPagingItems<FeedItem.ReplyItem>,
    lovedItems: LazyPagingItems<FeedItem>,
    viewModel: UserProfileViewModel,
    onEvent: (UserProfileUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    // Show title only when collapsed
                    Text(
                        text = uiState.user?.displayName ?: "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(UserProfileUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.user == null -> {
                LoadingView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.user == null -> {
                EmptyView(
                    message = "Profile not found",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                UserProfileScrollableContent(
                    uiState = uiState,
                    posts = posts,
                    replies = replies,
                    lovedItems = lovedItems,
                    viewModel = viewModel,
                    onEvent = onEvent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun UserProfileScrollableContent(
    uiState: UserProfileUiState,
    posts: LazyPagingItems<Post>,
    replies: LazyPagingItems<FeedItem.ReplyItem>,
    lovedItems: LazyPagingItems<FeedItem>,
    viewModel: UserProfileViewModel,
    onEvent: (UserProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
    ) {
        // Profile Header - will collapse when scrolling
        item {
            UserProfileHeaderSection(
                uiState = uiState,
                onFollowClick = { onEvent(UserProfileUiEvent.ToggleFollow) }
            )
        }

        // Sticky Tab Row
        stickyHeader {
            ProfileTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { onEvent(UserProfileUiEvent.TabSelected(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tab Content
        when (uiState.selectedTab) {
            UserProfileTab.POSTS -> {
                // Posts grid content (3 columns)
                val postsList = (0 until posts.itemCount).mapNotNull { posts[it] }
                val chunkedPosts = postsList.chunked(3)

                items(
                    count = chunkedPosts.size,
                    key = { index -> "row_$index" }
                ) { rowIndex ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start
                    ) {
                        chunkedPosts[rowIndex].forEach { post ->
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                id.usecase.meetcat.presentation.screen.search.component.SearchPostGridItem(
                                    post = post,
                                    onClick = { onEvent(UserProfileUiEvent.NavigateToPost(post.id)) }
                                )
                            }
                        }
                        // Fill remaining cells if row is not full
                        repeat(3 - chunkedPosts[rowIndex].size) {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Loading state for posts
                posts.loadState.append.let { appendState ->
                    when (appendState) {
                        is androidx.paging.LoadState.Loading -> {
                            item {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            UserProfileTab.REPLIES -> {
                // Replies list content
                items(
                    count = replies.itemCount,
                    key = { index -> replies[index]?.reply?.id ?: "reply_$index" }
                ) { index ->
                    replies[index]?.let { replyItem ->
                        id.usecase.meetcat.presentation.component.card.ReplyCard(
                            reply = replyItem.reply,
                            onReplyClick = { onEvent(UserProfileUiEvent.NavigateToPost(replyItem.reply.id)) },
                            onProfileClick = { },
                            onOriginalPostClick = { onEvent(UserProfileUiEvent.NavigateToPost(replyItem.reply.originalPostId)) },
                            onOriginalProfileClick = { },
                            onLoveClick = { onEvent(UserProfileUiEvent.LoveReply(replyItem.reply.id)) },
                            onCommentClick = { },
                            onShareClick = { },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Loading state for replies
                replies.loadState.append.let { appendState ->
                    when (appendState) {
                        is androidx.paging.LoadState.Loading -> {
                            item {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            UserProfileTab.LOVED -> {
                // Loved items content
                items(
                    count = lovedItems.itemCount,
                    key = { index ->
                        when (val item = lovedItems[index]) {
                            is FeedItem.PostItem -> "post_${item.post.id}"
                            is FeedItem.ReplyItem -> "reply_${item.reply.id}"
                            null -> "loved_$index"
                        }
                    }
                ) { index ->
                    lovedItems[index]?.let { item ->
                        when (item) {
                            is FeedItem.PostItem -> {
                                id.usecase.meetcat.presentation.component.card.PostCard(
                                    post = item.post,
                                    onPostClick = { onEvent(UserProfileUiEvent.NavigateToPost(item.post.id)) },
                                    onProfileClick = { },
                                    onLoveClick = { onEvent(UserProfileUiEvent.LovePost(item.post.id)) },
                                    onCommentClick = { },
                                    onReplyClick = { },
                                    onShareClick = { },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            is FeedItem.ReplyItem -> {
                                id.usecase.meetcat.presentation.component.card.ReplyCard(
                                    reply = item.reply,
                                    onReplyClick = { onEvent(UserProfileUiEvent.NavigateToPost(item.reply.id)) },
                                    onProfileClick = { },
                                    onOriginalPostClick = { onEvent(UserProfileUiEvent.NavigateToPost(item.reply.originalPostId)) },
                                    onOriginalProfileClick = { },
                                    onLoveClick = { onEvent(UserProfileUiEvent.LoveReply(item.reply.id)) },
                                    onCommentClick = { },
                                    onShareClick = { },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Loading state for loved items
                lovedItems.loadState.append.let { appendState ->
                    when (appendState) {
                        is androidx.paging.LoadState.Loading -> {
                            item {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator()
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeaderSection(
    uiState: UserProfileUiState,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // User info and stats
        UserProfileHeader(
            user = uiState.user!!,
            stats = listOf(
                ProfileStat(
                    count = uiState.user.postsCount,
                    label = "Posts"
                ),
                ProfileStat(
                    count = uiState.user.followersCount,
                    label = "Followers"
                ),
                ProfileStat(
                    count = uiState.user.followingCount,
                    label = "Following"
                )
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Follow/Unfollow Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.user.isFollowing) {
                OutlinedButton(
                    onClick = onFollowClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Following")
                }
            } else {
                Button(
                    onClick = onFollowClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Follow")
                }
            }
        }
    }
}

@Composable
private fun ProfileTabRow(
    selectedTab: UserProfileTab,
    onTabSelected: (UserProfileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab.ordinal
        ) {
            Tab(
                selected = selectedTab == UserProfileTab.POSTS,
                onClick = { onTabSelected(UserProfileTab.POSTS) },
                text = { Text("Posts") }
            )
            Tab(
                selected = selectedTab == UserProfileTab.REPLIES,
                onClick = { onTabSelected(UserProfileTab.REPLIES) },
                text = { Text("Replies") }
            )
            Tab(
                selected = selectedTab == UserProfileTab.LOVED,
                onClick = { onTabSelected(UserProfileTab.LOVED) },
                text = { Text("Loved") }
            )
        }
        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileScreenPreview() {
    MeetCatTheme {
        // Preview not available without ViewModel
    }
}
