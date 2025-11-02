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
import id.usecase.meetcat.ui.theme.MeetCatTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    exploreViewModel: ExploreViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val mainUiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val isBottomNavVisible = mainViewModel.isBottomNavVisible

    MainContent(
        mainUiState = mainUiState,
        isBottomNavVisible = isBottomNavVisible,
        onEvent = mainViewModel::onEvent,
        exploreViewModel = exploreViewModel,
        modifier = modifier
    )
}

@Composable
private fun MainContent(
    mainUiState: MainUiState,
    isBottomNavVisible: Boolean,
    onEvent: (MainUiEvent) -> Unit,
    exploreViewModel: ExploreViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main content - full screen, each screen handles its own bottom padding
        when (mainUiState.currentRoute) {
            BottomNavItem.Explore.route -> {
                ExploreScreenWithScrollDetection(
                    viewModel = exploreViewModel,
                    onShowBottomNav = { onEvent(MainUiEvent.ShowBottomNav) },
                    onHideBottomNav = { onEvent(MainUiEvent.HideBottomNav) }
                )
            }

            BottomNavItem.Search.route -> {
                PlaceholderScreen("Search")
            }

            BottomNavItem.Chat.route -> {
                PlaceholderScreen("Chat")
            }

            BottomNavItem.Profile.route -> {
                PlaceholderScreen("Profile")
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
    modifier: Modifier = Modifier
) {
    ExploreScreen(
        viewModel = viewModel,
        modifier = modifier,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav
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
            exploreViewModel = koinViewModel()
        )
    }
}
