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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.user?.displayName ?: "Profile",
                        style = MaterialTheme.typography.titleLarge
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { onEvent(UserProfileUiEvent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Profile Header with Follow Button
                        UserProfileHeaderSection(
                            uiState = uiState,
                            onFollowClick = { onEvent(UserProfileUiEvent.ToggleFollow) }
                        )

                        // Tab Row
                        ProfileTabRow(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = { onEvent(UserProfileUiEvent.TabSelected(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Tab Content
                        when (uiState.selectedTab) {
                            UserProfileTab.POSTS -> {
                                PostsGrid(
                                    posts = posts,
                                    viewModel = viewModel,
                                    onPostClick = { onEvent(UserProfileUiEvent.NavigateToPost(it)) },
                                    onShowBottomNav = {},
                                    onHideBottomNav = {}
                                )
                            }
                            UserProfileTab.REPLIES -> {
                                RepliesList(
                                    replies = replies,
                                    viewModel = viewModel,
                                    onReplyClick = { onEvent(UserProfileUiEvent.NavigateToPost(it)) },
                                    onLoveClick = { onEvent(UserProfileUiEvent.LoveReply(it)) },
                                    onShowBottomNav = {},
                                    onHideBottomNav = {}
                                )
                            }
                            UserProfileTab.LOVED -> {
                                LovedList(
                                    items = lovedItems,
                                    viewModel = viewModel,
                                    onPostClick = { onEvent(UserProfileUiEvent.NavigateToPost(it)) },
                                    onLovePostClick = { onEvent(UserProfileUiEvent.LovePost(it)) },
                                    onLoveReplyClick = { onEvent(UserProfileUiEvent.LoveReply(it)) },
                                    onShowBottomNav = {},
                                    onHideBottomNav = {}
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
