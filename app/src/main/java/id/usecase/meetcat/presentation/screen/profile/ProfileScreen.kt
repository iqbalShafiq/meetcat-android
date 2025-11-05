package id.usecase.meetcat.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.domain.model.FeedItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.presentation.component.card.PostCard
import id.usecase.meetcat.presentation.component.card.ReplyCard
import id.usecase.meetcat.presentation.component.state.EmptyView
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.component.user.ProfileStat
import id.usecase.meetcat.presentation.component.user.UserProfileHeader
import id.usecase.meetcat.presentation.screen.search.component.SearchPostGridItem
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
    onNavigateToPost: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.NavigateToPost -> {
                    onNavigateToPost(effect.postId)
                }
                is ProfileUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ProfileContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.user == null -> {
                LoadingView(modifier = Modifier.padding(paddingValues))
            }

            uiState.user == null -> {
                EmptyView(
                    message = "Profile not found",
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(ProfileUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Profile Header
                        UserProfileHeader(
                            user = uiState.user,
                            stats = listOf(
                                ProfileStat(
                                    count = uiState.user.postsCount,
                                    label = "Posts"
                                ),
                                ProfileStat(
                                    count = uiState.user.followingCount,
                                    label = "Following Cats"
                                ),
                                ProfileStat(
                                    count = calculateTotalLoves(uiState.user),
                                    label = "Loves"
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        )

                        // Tab Row
                        ProfileTabRow(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = { onEvent(ProfileUiEvent.TabSelected(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Tab Content
                        when (uiState.selectedTab) {
                            ProfileTab.POSTS -> {
                                PostsGrid(
                                    posts = uiState.posts,
                                    onPostClick = { onEvent(ProfileUiEvent.NavigateToPost(it)) },
                                    onShowBottomNav = onShowBottomNav,
                                    onHideBottomNav = onHideBottomNav
                                )
                            }
                            ProfileTab.REPLIES -> {
                                RepliesList(
                                    replies = uiState.replies,
                                    onReplyClick = { onEvent(ProfileUiEvent.NavigateToPost(it)) },
                                    onLoveClick = { onEvent(ProfileUiEvent.LoveReply(it)) },
                                    onShowBottomNav = onShowBottomNav,
                                    onHideBottomNav = onHideBottomNav
                                )
                            }
                            ProfileTab.LOVED -> {
                                LovedList(
                                    items = uiState.lovedItems,
                                    onPostClick = { onEvent(ProfileUiEvent.NavigateToPost(it)) },
                                    onLovePostClick = { onEvent(ProfileUiEvent.LovePost(it)) },
                                    onLoveReplyClick = { onEvent(ProfileUiEvent.LoveReply(it)) },
                                    onShowBottomNav = onShowBottomNav,
                                    onHideBottomNav = onHideBottomNav
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTabRow(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SecondaryTabRow(
            selectedTabIndex = selectedTab.ordinal
        ) {
            Tab(
                selected = selectedTab == ProfileTab.POSTS,
                onClick = { onTabSelected(ProfileTab.POSTS) },
                text = { Text("Posts") }
            )
            Tab(
                selected = selectedTab == ProfileTab.REPLIES,
                onClick = { onTabSelected(ProfileTab.REPLIES) },
                text = { Text("Replies") }
            )
            Tab(
                selected = selectedTab == ProfileTab.LOVED,
                onClick = { onTabSelected(ProfileTab.LOVED) },
                text = { Text("Loved") }
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun PostsGrid(
    posts: ImmutableList<Post>,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    if (posts.isEmpty()) {
        EmptyView(
            message = "No posts yet",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val lazyGridState = rememberLazyStaggeredGridState()
    var previousIndex by remember { mutableIntStateOf(0) }

    // Scroll detection
    LaunchedEffect(Unit) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { currentIndex: Int ->
                if (currentIndex < previousIndex) {
                    onShowBottomNav()
                } else if (currentIndex > previousIndex) {
                    onHideBottomNav()
                }
                previousIndex = currentIndex
            }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = 88.dp // 80dp navbar + 8dp spacing
        ),
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
private fun RepliesList(
    replies: ImmutableList<FeedItem.ReplyItem>,
    onReplyClick: (String) -> Unit,
    onLoveClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    if (replies.isEmpty()) {
        EmptyView(
            message = "No replies yet",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val lazyListState = rememberLazyListState()

    // Scroll detection
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
            bottom = 88.dp
        ),
        state = lazyListState
    ) {
        items(
            items = replies,
            key = { it.reply.id }
        ) { item ->
            ReplyCard(
                reply = item.reply,
                onReplyClick = { onReplyClick(item.reply.id) },
                onProfileClick = { /* TODO */ },
                onOriginalPostClick = { onReplyClick(item.reply.originalPostId) },
                onOriginalProfileClick = { /* TODO */ },
                onLoveClick = { onLoveClick(item.reply.id) },
                onCommentClick = { /* TODO */ },
                onShareClick = { /* TODO */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun LovedList(
    items: ImmutableList<FeedItem>,
    onPostClick: (String) -> Unit,
    onLovePostClick: (String) -> Unit,
    onLoveReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    if (items.isEmpty()) {
        EmptyView(
            message = "No loved items yet",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    val lazyListState = rememberLazyListState()

    // Scroll detection
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
            bottom = 88.dp
        ),
        state = lazyListState
    ) {
        items(
            items = items,
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
                        onPostClick = { onPostClick(item.post.id) },
                        onProfileClick = { /* TODO */ },
                        onLoveClick = { onLovePostClick(item.post.id) },
                        onCommentClick = { /* TODO */ },
                        onReplyClick = { /* TODO */ },
                        onShareClick = { /* TODO */ },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                is FeedItem.ReplyItem -> {
                    ReplyCard(
                        reply = item.reply,
                        onReplyClick = { onPostClick(item.reply.id) },
                        onProfileClick = { /* TODO */ },
                        onOriginalPostClick = { onPostClick(item.reply.originalPostId) },
                        onOriginalProfileClick = { /* TODO */ },
                        onLoveClick = { onLoveReplyClick(item.reply.id) },
                        onCommentClick = { /* TODO */ },
                        onShareClick = { /* TODO */ },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// Helper functions
private fun calculateTotalLoves(user: User): Int {
    // This would ideally come from the backend
    // For now, we'll use a simple calculation based on posts count
    return user.postsCount * 25
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MeetCatTheme {
        ProfileContent(
            uiState = ProfileUiState(
                user = User(
                    id = "1",
                    username = "catwhiskerer",
                    displayName = "Cat Whisperer",
                    bio = "Passionate about cats and photography 🐱📸",
                    profileImageUrl = null,
                    followersCount = 2456,
                    followingCount = 342,
                    postsCount = 156,
                    isFollowing = false,
                    createdAt = System.currentTimeMillis()
                ),
                posts = persistentListOf()
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
