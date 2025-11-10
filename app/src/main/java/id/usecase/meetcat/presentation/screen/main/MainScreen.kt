package id.usecase.meetcat.presentation.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.presentation.common.SnackbarController
import id.usecase.meetcat.presentation.common.SnackbarEvent
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
import id.usecase.meetcat.presentation.screen.settings.about.AboutScreen
import id.usecase.meetcat.presentation.screen.settings.about.AboutViewModel
import id.usecase.meetcat.presentation.screen.settings.account.AccountSettingsScreen
import id.usecase.meetcat.presentation.screen.settings.account.AccountSettingsViewModel
import id.usecase.meetcat.presentation.screen.settings.privacy.PrivacySettingsScreen
import id.usecase.meetcat.presentation.screen.settings.privacy.PrivacySettingsViewModel
import id.usecase.meetcat.presentation.screen.userprofile.UserProfileScreen
import id.usecase.meetcat.presentation.screen.createpost.CreatePostScreen
import id.usecase.meetcat.presentation.screen.createpost.CreatePostUiEvent
import id.usecase.meetcat.presentation.screen.createpost.CreatePostViewModel
import id.usecase.meetcat.presentation.screen.createreply.CreateReplyScreen
import id.usecase.meetcat.presentation.screen.createreply.CreateReplyUiEvent
import id.usecase.meetcat.presentation.screen.createreply.CreateReplyViewModel
import id.usecase.meetcat.presentation.screen.editpost.EditPostScreen
import id.usecase.meetcat.presentation.screen.editpost.EditPostViewModel
import id.usecase.meetcat.presentation.screen.editprofile.EditProfileScreen
import id.usecase.meetcat.presentation.screen.editprofile.EditProfileViewModel
import id.usecase.meetcat.presentation.screen.followerslist.FollowersListScreen
import id.usecase.meetcat.presentation.screen.followerslist.FollowersListViewModel
import id.usecase.meetcat.presentation.screen.followinglist.FollowingListScreen
import id.usecase.meetcat.presentation.screen.followinglist.FollowingListViewModel
import id.usecase.meetcat.presentation.screen.camera.CameraScreen
import id.usecase.meetcat.ui.theme.MeetCatTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MainScreen(
    onNavigateToLogin: () -> Unit,
    mainViewModel: MainViewModel = koinViewModel(),
    exploreViewModel: ExploreViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
    mapsViewModel: MapsViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    aboutViewModel: AboutViewModel = koinViewModel(),
    accountSettingsViewModel: AccountSettingsViewModel = koinViewModel(),
    privacySettingsViewModel: PrivacySettingsViewModel = koinViewModel(),
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
        aboutViewModel = aboutViewModel,
        accountSettingsViewModel = accountSettingsViewModel,
        privacySettingsViewModel = privacySettingsViewModel,
        onNavigateToLogin = onNavigateToLogin,
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
    aboutViewModel: AboutViewModel,
    accountSettingsViewModel: AccountSettingsViewModel,
    privacySettingsViewModel: PrivacySettingsViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Handle system back button
    BackHandler(enabled = mainUiState.backStack.size > 1) {
        onEvent(MainUiEvent.NavigateBack)
    }

    // Global snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Listen to global snackbar events
    LaunchedEffect(Unit) {
        SnackbarController.snackbarEvents.collect { event ->
            when (event) {
                is SnackbarEvent.Error -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        withDismissAction = true
                    )

                    // Handle retry action
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onRetry?.invoke()
                    }
                }
                is SnackbarEvent.Message -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        withDismissAction = true
                    )

                    // Handle action click
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onActionClick?.invoke()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Main content with smooth transitions
        val bottomNavRoutes = listOf(
            BottomNavItem.Explore.route,
            BottomNavItem.Search.route,
            BottomNavItem.NearMe.route,
            BottomNavItem.Profile.route
        )
        val isBottomNavRoute = bottomNavRoutes.contains(mainUiState.currentRoute)

        // Reset isNavigatingBack after route changes
        LaunchedEffect(mainUiState.currentRoute) {
            if (mainUiState.isNavigatingBack) {
                // Give time for transition to start, then reset
                kotlinx.coroutines.delay(50)
                onEvent(MainUiEvent.ResetNavigationDirection)
            }
        }

        AnimatedContent(
            targetState = mainUiState.currentRoute,
            transitionSpec = {
                if (isBottomNavRoute) {
                    // Bottom nav tabs: Simple crossfade for minimal transition
                    fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                } else {
                    // Detail/nested screens: Smooth slide + fade
                    // Reverse direction when navigating back
                    if (mainUiState.isNavigatingBack) {
                        // Back: slide from left to right
                        fadeIn(animationSpec = tween(300)) +
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { fullWidth -> -fullWidth / 8 }
                            ) togetherWith
                            fadeOut(animationSpec = tween(300)) +
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { fullWidth -> fullWidth / 8 }
                            )
                    } else {
                        // Forward: slide from right to left
                        fadeIn(animationSpec = tween(300)) +
                            slideInHorizontally(
                                animationSpec = tween(300),
                                initialOffsetX = { fullWidth -> fullWidth / 8 }
                            ) togetherWith
                            fadeOut(animationSpec = tween(300)) +
                            slideOutHorizontally(
                                animationSpec = tween(300),
                                targetOffsetX = { fullWidth -> -fullWidth / 8 }
                            )
                    }
                }
            },
            label = "MainScreenTransition"
        ) { currentRoute ->
        when {
            currentRoute == BottomNavItem.Explore.route -> {
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
                    },
                    onNavigateToCreatePost = {
                        onEvent(MainUiEvent.NavigateTo("create_post"))
                    },
                    onNavigateToEditPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("edit_post/$postId"))
                    },
                    isBottomNavVisible = isBottomNavVisible
                )
            }

            currentRoute == BottomNavItem.Search.route -> {
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

            currentRoute == BottomNavItem.NearMe.route -> {
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

            currentRoute == BottomNavItem.Profile.route -> {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onShowBottomNav = { onEvent(MainUiEvent.ShowBottomNav) },
                    onHideBottomNav = { onEvent(MainUiEvent.HideBottomNav) },
                    onNavigateToPost = { postId ->
                        onEvent(MainUiEvent.NavigateTo("post_detail/$postId"))
                    },
                    onNavigateToSettings = {
                        onEvent(MainUiEvent.NavigateTo("settings"))
                    },
                    onNavigateToEditProfile = {
                        onEvent(MainUiEvent.NavigateTo("edit_profile"))
                    },
                    onNavigateToFollowers = { userId ->
                        onEvent(MainUiEvent.NavigateTo("followers_list/$userId"))
                    },
                    onNavigateToFollowing = { userId ->
                        onEvent(MainUiEvent.NavigateTo("following_list/$userId"))
                    }
                )
            }

            currentRoute == "settings" -> {
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
                    onNavigateToLogin = onNavigateToLogin
                )
            }

            currentRoute == "account_settings" -> {
                AccountSettingsScreen(
                    viewModel = accountSettingsViewModel,
                    onNavigateBack = { onEvent(MainUiEvent.NavigateBack) }
                )
            }

            currentRoute == "privacy_settings" -> {
                PrivacySettingsScreen(
                    viewModel = privacySettingsViewModel,
                    onNavigateBack = { onEvent(MainUiEvent.NavigateBack) }
                )
            }

            currentRoute == "about" -> {
                AboutScreen(
                    viewModel = aboutViewModel,
                    onNavigateBack = { onEvent(MainUiEvent.NavigateBack) }
                )
            }

            currentRoute.startsWith("post_detail/") -> {
                val postId = currentRoute.substringAfter("post_detail/")
                PostDetailScreen(
                    postId = postId,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToProfile = { userId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$userId"))
                    },
                    onNavigateToCreateReply = { replyPostId ->
                        onEvent(MainUiEvent.NavigateTo("create_reply/$replyPostId"))
                    }
                )
            }

            currentRoute.startsWith("reply_detail/") -> {
                val replyId = currentRoute.substringAfter("reply_detail/")
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

            currentRoute.startsWith("user_profile/") -> {
                val userId = currentRoute.substringAfter("user_profile/")
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

            currentRoute == "create_post" -> {
                val createPostViewModel: CreatePostViewModel = koinViewModel()

                // Handle selected image from camera/gallery
                LaunchedEffect(mainUiState.selectedImageUri) {
                    mainUiState.selectedImageUri?.let { uri ->
                        createPostViewModel.onEvent(CreatePostUiEvent.MediaSelected(uri))
                        onEvent(MainUiEvent.ClearSelectedImage)
                    }
                }

                CreatePostScreen(
                    viewModel = createPostViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToPostDetail = { postId ->
                        onEvent(MainUiEvent.NavigateToAndReplace("post_detail/$postId"))
                    },
                    onNavigateToCamera = {
                        onEvent(MainUiEvent.NavigateTo("camera"))
                    }
                )
            }

            currentRoute.startsWith("edit_post/") -> {
                val postId = currentRoute.substringAfter("edit_post/")
                val editPostViewModel: EditPostViewModel = koinViewModel(key = "editPost_$postId") { parametersOf(postId) }
                EditPostScreen(
                    viewModel = editPostViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToPostDetail = { detailPostId ->
                        onEvent(MainUiEvent.NavigateToAndReplace("post_detail/$detailPostId"))
                    }
                )
            }

            currentRoute.startsWith("create_reply/") -> {
                val postId = currentRoute.substringAfter("create_reply/")
                val createReplyViewModel: CreateReplyViewModel = koinViewModel(key = "createReply_$postId") { parametersOf(postId) }

                // Handle selected image from camera/gallery
                LaunchedEffect(mainUiState.selectedImageUri) {
                    mainUiState.selectedImageUri?.let { uri ->
                        createReplyViewModel.onEvent(CreateReplyUiEvent.MediaSelected(uri))
                        onEvent(MainUiEvent.ClearSelectedImage)
                    }
                }

                CreateReplyScreen(
                    viewModel = createReplyViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToReplyDetail = { replyId ->
                        onEvent(MainUiEvent.NavigateToAndReplace("reply_detail/$replyId"))
                    },
                    onNavigateToCamera = {
                        onEvent(MainUiEvent.NavigateTo("camera"))
                    }
                )
            }

            currentRoute == "edit_profile" -> {
                val editProfileViewModel: EditProfileViewModel = koinViewModel()
                EditProfileScreen(
                    viewModel = editProfileViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    }
                )
            }

            currentRoute.startsWith("followers_list/") -> {
                val userId = currentRoute.substringAfter("followers_list/")
                val followersListViewModel: FollowersListViewModel = koinViewModel(key = "followersList_$userId") { parametersOf(userId) }
                FollowersListScreen(
                    viewModel = followersListViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToProfile = { profileUserId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$profileUserId"))
                    }
                )
            }

            currentRoute.startsWith("following_list/") -> {
                val userId = currentRoute.substringAfter("following_list/")
                val followingListViewModel: FollowingListViewModel = koinViewModel(key = "followingList_$userId") { parametersOf(userId) }
                FollowingListScreen(
                    viewModel = followingListViewModel,
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onNavigateToProfile = { profileUserId ->
                        onEvent(MainUiEvent.NavigateTo("user_profile/$profileUserId"))
                    }
                )
            }

            currentRoute == "camera" -> {
                CameraScreen(
                    onNavigateBack = {
                        onEvent(MainUiEvent.NavigateBack)
                    },
                    onImageCaptured = { uri ->
                        // Store the captured image URI and navigate back
                        onEvent(MainUiEvent.ImageSelected(uri))
                        onEvent(MainUiEvent.NavigateBack)
                    }
                )
            }
        }
        }

            // Navbar overlay at bottom
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
}

@Composable
private fun ExploreScreenWithScrollDetection(
    viewModel: ExploreViewModel,
    onShowBottomNav: () -> Unit,
    onHideBottomNav: () -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToReply: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToEditPost: (String) -> Unit,
    isBottomNavVisible: Boolean,
    modifier: Modifier = Modifier
) {
    ExploreScreen(
        viewModel = viewModel,
        modifier = modifier,
        onShowBottomNav = onShowBottomNav,
        onHideBottomNav = onHideBottomNav,
        onNavigateToPost = onNavigateToPost,
        onNavigateToReply = onNavigateToReply,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToCreatePost = onNavigateToCreatePost,
        onNavigateToEditPost = onNavigateToEditPost,
        isBottomNavVisible = isBottomNavVisible
    )
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
            settingsViewModel = koinViewModel(),
            aboutViewModel = koinViewModel(),
            accountSettingsViewModel = koinViewModel(),
            privacySettingsViewModel = koinViewModel(),
            onNavigateToLogin = {}
        )
    }
}
