package id.usecase.meetcat.presentation.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect Paging3 flows for each tab
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val replies = viewModel.replies.collectAsLazyPagingItems()
    val lovedItems = viewModel.lovedItems.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.NavigateToPost -> {
                    onNavigateToPost(effect.postId)
                }
                is ProfileUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ProfileUiEffect.NavigateToSettings -> {
                    onNavigateToSettings()
                }
            }
        }
    }

    ProfileContent(
        uiState = uiState,
        posts = posts,
        replies = replies,
        lovedItems = lovedItems,
        viewModel = viewModel,
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
    posts: LazyPagingItems<Post>,
    replies: LazyPagingItems<FeedItem.ReplyItem>,
    lovedItems: LazyPagingItems<FeedItem>,
    viewModel: ProfileViewModel,
    onEvent: (ProfileUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {},
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
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                actions = {
                    IconButton(onClick = { onEvent(ProfileUiEvent.NavigateToSettings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
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
                LoadingView(modifier = Modifier.padding(paddingValues))
            }

            uiState.user == null -> {
                EmptyView(
                    message = "Profile not found",
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                ProfileScrollableContent(
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
private fun ProfileScrollableContent(
    uiState: ProfileUiState,
    posts: LazyPagingItems<Post>,
    replies: LazyPagingItems<FeedItem.ReplyItem>,
    lovedItems: LazyPagingItems<FeedItem>,
    viewModel: ProfileViewModel,
    onEvent: (ProfileUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier
    ) {
        // Profile Header - will collapse when scrolling
        item {
            UserProfileHeader(
                user = uiState.user!!,
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
        }

        // Sticky Tab Row
        stickyHeader {
            ProfileTabRow(
                selectedTab = uiState.selectedTab,
                onTabSelected = { onEvent(ProfileUiEvent.TabSelected(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tab Content
        when (uiState.selectedTab) {
            ProfileTab.POSTS -> {
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
                                    onClick = { onEvent(ProfileUiEvent.NavigateToPost(post.id)) }
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

            ProfileTab.REPLIES -> {
                // Replies list content
                items(
                    count = replies.itemCount,
                    key = { index -> replies[index]?.reply?.id ?: "reply_$index" }
                ) { index ->
                    replies[index]?.let { replyItem ->
                        id.usecase.meetcat.presentation.component.card.ReplyCard(
                            reply = replyItem.reply,
                            onReplyClick = { onEvent(ProfileUiEvent.NavigateToPost(replyItem.reply.id)) },
                            onProfileClick = { },
                            onOriginalPostClick = { onEvent(ProfileUiEvent.NavigateToPost(replyItem.reply.originalPostId)) },
                            onOriginalProfileClick = { },
                            onLoveClick = { onEvent(ProfileUiEvent.LoveReply(replyItem.reply.id)) },
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

            ProfileTab.LOVED -> {
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
                                    onPostClick = { onEvent(ProfileUiEvent.NavigateToPost(item.post.id)) },
                                    onProfileClick = { },
                                    onLoveClick = { onEvent(ProfileUiEvent.LovePost(item.post.id)) },
                                    onCommentClick = { },
                                    onReplyClick = { },
                                    onShareClick = { },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            is FeedItem.ReplyItem -> {
                                id.usecase.meetcat.presentation.component.card.ReplyCard(
                                    reply = item.reply,
                                    onReplyClick = { onEvent(ProfileUiEvent.NavigateToPost(item.reply.id)) },
                                    onProfileClick = { },
                                    onOriginalPostClick = { onEvent(ProfileUiEvent.NavigateToPost(item.reply.originalPostId)) },
                                    onOriginalProfileClick = { },
                                    onLoveClick = { onEvent(ProfileUiEvent.LoveReply(item.reply.id)) },
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

// Interface for ViewModels that support scroll position tracking
interface ScrollableViewModel {
    var postsScrollIndex: Int
    var repliesScrollIndex: Int
    var lovedScrollIndex: Int
}

@Composable
internal fun PostsGrid(
    posts: LazyPagingItems<Post>,
    viewModel: ScrollableViewModel,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    // Use remember with viewModel as key to preserve state across navigation
    val lazyGridState = remember(viewModel) {
        androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState(
            initialFirstVisibleItemIndex = viewModel.postsScrollIndex
        )
    }
    var previousIndex by remember { mutableIntStateOf(0) }

    // Save scroll position to ViewModel continuously
    LaunchedEffect(Unit) {
        snapshotFlow { lazyGridState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.postsScrollIndex = index
            }
    }

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

    // Show empty state if no items loaded
    if (posts.loadState.refresh is LoadState.NotLoading && posts.itemCount == 0) {
        EmptyView(
            message = "No posts yet",
            modifier = modifier.fillMaxSize()
        )
        return
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
            count = posts.itemCount,
            key = posts.itemKey { it.id }
        ) { index ->
            posts[index]?.let { post ->
                SearchPostGridItem(
                    post = post,
                    onClick = { onPostClick(post.id) }
                )
            }
        }

        // Handle loading state for infinite scroll
        posts.loadState.append.let { appendState ->
            when (appendState) {
                is LoadState.Loading -> {
                    item {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Button(onClick = { posts.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
internal fun RepliesList(
    replies: LazyPagingItems<FeedItem.ReplyItem>,
    viewModel: ScrollableViewModel,
    onReplyClick: (String) -> Unit,
    onLoveClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    // Use remember with viewModel as key to preserve state across navigation
    val lazyListState = remember(viewModel) {
        androidx.compose.foundation.lazy.LazyListState(
            firstVisibleItemIndex = viewModel.repliesScrollIndex
        )
    }

    // Save scroll position to ViewModel continuously
    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.repliesScrollIndex = index
            }
    }

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

    // Show empty state if no items loaded
    if (replies.loadState.refresh is LoadState.NotLoading && replies.itemCount == 0) {
        EmptyView(
            message = "No replies yet",
            modifier = modifier.fillMaxSize()
        )
        return
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
            count = replies.itemCount,
            key = replies.itemKey { it.reply.id }
        ) { index ->
            replies[index]?.let { item ->
                ReplyCard(
                    reply = item.reply,
                    onReplyClick = { onReplyClick(item.reply.id) },
                    onProfileClick = { /* Current user's profile - already on profile screen */ },
                    onOriginalPostClick = { onReplyClick(item.reply.originalPostId) },
                    onOriginalProfileClick = { /* Navigate to original post author - not implemented yet */ },
                    onLoveClick = { onLoveClick(item.reply.id) },
                    onCommentClick = { /* Show comment dialog - not implemented yet */ },
                    onShareClick = { /* Share reply - not implemented yet */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Handle loading state for infinite scroll
        replies.loadState.append.let { appendState ->
            when (appendState) {
                is LoadState.Loading -> {
                    item {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Button(onClick = { replies.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
internal fun LovedList(
    items: LazyPagingItems<FeedItem>,
    viewModel: ScrollableViewModel,
    onPostClick: (String) -> Unit,
    onLovePostClick: (String) -> Unit,
    onLoveReplyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onShowBottomNav: () -> Unit = {},
    onHideBottomNav: () -> Unit = {}
) {
    // Use remember with viewModel as key to preserve state across navigation
    val lazyListState = remember(viewModel) {
        androidx.compose.foundation.lazy.LazyListState(
            firstVisibleItemIndex = viewModel.lovedScrollIndex
        )
    }

    // Save scroll position to ViewModel continuously
    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.lovedScrollIndex = index
            }
    }

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

    // Show empty state if no items loaded
    if (items.loadState.refresh is LoadState.NotLoading && items.itemCount == 0) {
        EmptyView(
            message = "No loved items yet",
            modifier = modifier.fillMaxSize()
        )
        return
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
            count = items.itemCount,
            key = items.itemKey { item ->
                when (item) {
                    is FeedItem.PostItem -> "post_${item.post.id}"
                    is FeedItem.ReplyItem -> "reply_${item.reply.id}"
                }
            },
            contentType = { index ->
                items.peek(index)?.let { item ->
                    when (item) {
                        is FeedItem.PostItem -> "post"
                        is FeedItem.ReplyItem -> "reply"
                    }
                }
            }
        ) { index ->
            items[index]?.let { item ->
                when (item) {
                    is FeedItem.PostItem -> {
                        PostCard(
                            post = item.post,
                            onPostClick = { onPostClick(item.post.id) },
                            onProfileClick = { /* Navigate to post author profile - not implemented yet */ },
                            onLoveClick = { onLovePostClick(item.post.id) },
                            onCommentClick = { /* Show comment dialog - not implemented yet */ },
                            onReplyClick = { /* Show reply dialog - not implemented yet */ },
                            onShareClick = { /* Share post - not implemented yet */ },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    is FeedItem.ReplyItem -> {
                        ReplyCard(
                            reply = item.reply,
                            onReplyClick = { onPostClick(item.reply.id) },
                            onProfileClick = { /* Navigate to reply author profile - not implemented yet */ },
                            onOriginalPostClick = { onPostClick(item.reply.originalPostId) },
                            onOriginalProfileClick = { /* Navigate to original post author - not implemented yet */ },
                            onLoveClick = { onLoveReplyClick(item.reply.id) },
                            onCommentClick = { /* Show comment dialog - not implemented yet */ },
                            onShareClick = { /* Share reply - not implemented yet */ },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Handle loading state for infinite scroll
        items.loadState.append.let { appendState ->
            when (appendState) {
                is LoadState.Loading -> {
                    item {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is LoadState.Error -> {
                    item {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Button(onClick = { items.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {}
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

// Note: Previews for ProfileContent are not included as they require LazyPagingItems
// which cannot be easily mocked in Compose previews.
