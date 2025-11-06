package id.usecase.meetcat.presentation.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import id.usecase.meetcat.presentation.screen.settings.SettingsScreen
import id.usecase.meetcat.presentation.screen.settings.SettingsViewModel
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
    settingsViewModel: SettingsViewModel = koinViewModel(),
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
        settingsViewModel = settingsViewModel,
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
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler(enabled = mainUiState.backStack.size > 1) {
        onEvent(MainUiEvent.NavigateBack)
    }

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
                    },
                    onNavigateToProfile = { userId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$userId"))
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
                        onEvent(MainUiEvent.NavigateTo("user_profile/$userId"))
                    },
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onShowBottomNav = { onEvent(MainUiEvent.ShowBottomNav) },
                    onHideBottomNav = { onEvent(MainUiEvent.HideBottomNav) }
                )
            }

            mainUiState.currentRoute == BottomNavItem.NearMe.route -> {
                MapsScreen(
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    },
                    onNavigateToProfile = { userId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$userId"))
                    },
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
                    },
                    onNavigateToSettings = {
                        onEvent(MainUiEvent.NavigateTo("settings"))
                    }
                )
            }

            mainUiState.currentRoute == "settings" -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToAccountSettings = {
                        onEvent(MainUiEvent.NavigateTo("account_settings"))
                    },
                    onNavigateToPrivacySettings = {
                        onEvent(MainUiEvent.NavigateTo("privacy_settings"))
                    },
                    onNavigateToAbout = {
                        onEvent(MainUiEvent.NavigateTo("about"))
                    },
                    onNavigateToLogin = {
                        // TODO: Handle logout and navigate to login
                    }
                )
            }

            mainUiState.currentRoute == "account_settings" -> {
                PlaceholderSettingsScreen(
                    title = "Account Settings",
                    onNavigateBack = { onEvent(MainUiEvent.NavigateBack) }
                )
            }

            mainUiState.currentRoute == "privacy_settings" -> {
                PlaceholderSettingsScreen(
                    title = "Privacy Settings",
                    onNavigateBack = { onEvent(MainUiEvent.NavigateBack) }
                )
            }

            mainUiState.currentRoute == "about" -> {
                AboutScreen(
                    onNavigateBack = { onEvent(MainUiEvent.NavigateBack) }
                )
            }

            mainUiState.currentRoute.startsWith("post_detail/") -> {
                val postId = mainUiState.currentRoute.substringAfter("post_detail/")
                PostDetailScreen(
                    postId = postId,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
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
                        onEvent(MainUiEvent.NavigateBack)
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
                        onEvent(MainUiEvent.NavigateBack)
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
    onNavigateToProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ExploreScreen(
        viewModel = viewModel,
        modifier = modifier,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav,
        onNavigateToPost = onNavigateToPost,
        onNavigateToReply = onNavigateToReply,
        onNavigateToProfile = onNavigateToProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderSettingsScreen(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Coming soon",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About MeetCat",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MeetCat",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "A social platform for cat lovers to share and discover amazing cat content.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp)
            )
            Text(
                text = "© 2024 MeetCat. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 48.dp)
            )
        }
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
            profileViewModel = koinViewModel(),
            settingsViewModel = koinViewModel()
        )
    }
}
