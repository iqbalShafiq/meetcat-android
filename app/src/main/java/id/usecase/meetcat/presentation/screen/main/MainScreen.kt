package id.usecase.meetcat.presentation.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.presentation.component.navigation.BottomNavItem
import id.usecase.meetcat.presentation.component.navigation.MeetCatBottomNavBar
import id.usecase.meetcat.presentation.screen.explore.ExploreScreen
import id.usecase.meetcat.presentation.screen.explore.ExploreViewModel
import id.usecase.meetcat.presentation.screen.maps.MapsScreen
import id.usecase.meetcat.presentation.screen.maps.MapsViewModel
import id.usecase.meetcat.presentation.screen.postdetail.PostDetailScreen
import id.usecase.meetcat.presentation.screen.profile.ProfileScreen
import id.usecase.meetcat.presentation.screen.profile.ProfileViewModel
import id.usecase.meetcat.presentation.screen.replydetail.ReplyDetailScreen
import id.usecase.meetcat.presentation.screen.search.SearchScreen
import id.usecase.meetcat.presentation.screen.search.SearchViewModel
import id.usecase.meetcat.presentation.screen.userprofile.UserProfileScreen
import id.usecase.meetcat.ui.theme.MeetCatTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    exploreViewModel: ExploreViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
    mapsViewModel: MapsViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val isBottomNavVisible = mainViewModel.isBottomNavVisible

    MainContent(
        mainUiState = mainUiState,
        isBottomNavVisible = isBottomNavVisible,
        onEvent = mainViewModel::onEvent,
        exploreViewModel = exploreViewModel,
        searchViewModel = searchViewModel,
        mapsViewModel = mapsViewModel,
        profileViewModel = profileViewModel,
        modifier = modifier
    )
}

@Composable
private fun MainContent(
    mainUiState: MainUiState,
    isBottomNavVisible: Boolean,
    onEvent: (MainUiEvent) -> Unit,
    exploreViewModel: ExploreViewModel,
    searchViewModel: SearchViewModel,
    mapsViewModel: MapsViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main content - full screen, each screen handles its own bottom padding
        when {
            mainUiState.currentRoute == BottomNavItem.Explore.route -> {
                ExploreScreenWithScrollDetection(
                    viewModel = exploreViewModel,
                    onShowBottomNav = { onEvent(MainUiEvent.ShowBottomNav) },
                    onHideBottomNav = { onEvent(MainUiEvent.HideBottomNav) },
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    },
                    onNavigateToReply = { replyId ->
                        onEvent(MainUiEvent.NavigateTo("reply_detail/$replyId"))
                    }
                )
            }

            mainUiState.currentRoute == BottomNavItem.Search.route -> {
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    },
                    onNavigateToProfile = { userId ->
                        // TODO: Navigate to profile
                    },
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateTo(BottomNavItem.Explore.route))
                    },
                    onShowBottomNav = { onEvent(MainUiEvent.ShowBottomNav) },
                    onHideBottomNav = { onEvent(MainUiEvent.HideBottomNav) }
                )
            }

            mainUiState.currentRoute == BottomNavItem.NearMe.route -> {
                MapsScreen(
                    viewModel = mapsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }

            mainUiState.currentRoute == BottomNavItem.Profile.route -> {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onShowBottomNav = { onEvent(MainUiEvent.ShowBottomNav) },
                    onHideBottomNav = { onEvent(MainUiEvent.HideBottomNav) },
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    }
                )
            }

            mainUiState.currentRoute.startsWith("post_detail/") -> {
                val postId = mainUiState.currentRoute.substringAfter("post_detail/")
                PostDetailScreen(
                    postId = postId,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateTo(BottomNavItem.Explore.route))
                    },
                    onNavigateToProfile = { userId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$userId"))
                    }
                )
            }

            mainUiState.currentRoute.startsWith("reply_detail/") -> {
                val replyId = mainUiState.currentRoute.substringAfter("reply_detail/")
                ReplyDetailScreen(
                    replyId = replyId,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateTo(BottomNavItem.Explore.route))
                    },
                    onNavigateToProfile = { userId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$userId"))
                    },
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    }
                )
            }

            mainUiState.currentRoute.startsWith("user_profile/") -> {
                val userId = mainUiState.currentRoute.substringAfter("user_profile/")
                UserProfileScreen(
                    userId = userId,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateTo(BottomNavItem.Explore.route))
                    },
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    }
                )
            }

            else -> {
                PlaceholderScreen("Unknown")
            }
        }

        // Navbar overlay at bottom (outside Scaffold to avoid padding issues)
        MeetCatBottomNavBar(
            currentRoute = mainUiState.currentRoute,
            onNavigate = { route ->
                onEvent(MainUiEvent.NavigateTo(route))
            },
            isVisible = isBottomNavVisible,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ExploreScreenWithScrollDetection(
    viewModel: ExploreViewModel,
    onShowBottomNav: () -> Unit,
    onHideBottomNav: () -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExploreScreen(
        viewModel = viewModel,
        modifier = modifier,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav,
        onNavigateToPost = onNavigateToPost,
        onNavigateToReply = onNavigateToReply
    )
}

@Composable
private fun PlaceholderScreen(
    screenName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$screenName Screen\n(Coming soon)",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MeetCatTheme {
        MainContent(
            mainUiState = MainUiState(
                currentRoute = "explore"
            ),
            isBottomNavVisible = true,
            onEvent = {},
            exploreViewModel = koinViewModel(),
            searchViewModel = koinViewModel(),
            mapsViewModel = koinViewModel(),
            profileViewModel = koinViewModel()
        )
    }
}
