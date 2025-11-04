package id.usecase.meetcat.presentation.screen.maps

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.ui.theme.MeetCatTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    viewModel: MapsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is MapsUiEffect.RequestLocationPermission -> {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                is MapsUiEffect.ShowLocationPermissionRationale -> {
                    snackbarHostState.showSnackbar("Location permission is required to show nearby posts")
                }

                is MapsUiEffect.NavigateToPost -> {
                    // TODO: Navigate to post detail when implemented
                }

                is MapsUiEffect.NavigateToProfile -> {
                    // TODO: Navigate to profile when implemented
                }

                is MapsUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    MapsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
private fun MapsContent(
    uiState: MapsUiState,
    onEvent: (MapsUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentLocation = uiState.currentLocation

    val cameraPositionState = rememberCameraPositionState {
        if (currentLocation != null) {
            position = CameraPosition.fromLatLngZoom(
                LatLng(currentLocation.latitude, currentLocation.longitude),
                14f
            )
        }
    }

    // Animate camera to selected marker location
    LaunchedEffect(uiState.selectedPostId) {
        uiState.selectedPostId?.let { selectedId ->
            val selectedPost = uiState.nearbyPosts.find { it.id == selectedId }
            selectedPost?.location?.let { location ->
                cameraPositionState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            18f
                        )
                    )
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.hasLocationPermission) {
                FloatingActionButton(
                    onClick = { onEvent(MapsUiEvent.Refresh) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 80.dp)
        ) {
            when {
                !uiState.hasLocationPermission -> {
                    LocationPermissionRequiredView(
                        onRequestPermission = { onEvent(MapsUiEvent.RequestLocationPermission) }
                    )
                }

                uiState.isLoadingLocation -> {
                    LoadingView()
                }

                uiState.currentLocation != null -> {
                    MapView(
                        uiState = uiState,
                        onEvent = onEvent,
                        cameraPositionState = cameraPositionState
                    )
                }

                else -> {
                    ErrorView(
                        message = uiState.error ?: "Unable to get location",
                        onRetry = { onEvent(MapsUiEvent.LoadCurrentLocation) }
                    )
                }
            }

            // Show selected post detail bubble at bottom
            uiState.selectedPostId?.let { selectedId ->
                val selectedPost = uiState.nearbyPosts.find { it.id == selectedId }
                selectedPost?.let { post ->
                    PostDetailBubble(
                        post = post,
                        onDismiss = { onEvent(MapsUiEvent.DismissMarkerDetail) },
                        onLoveClick = { onEvent(MapsUiEvent.LovePost(post.id)) },
                        onProfileClick = { onEvent(MapsUiEvent.NavigateToProfile(post.userId)) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapView(
    uiState: MapsUiState,
    onEvent: (MapsUiEvent) -> Unit,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = uiState.hasLocationPermission
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = false
        )
    ) {
        // Add markers for each post with location
        uiState.nearbyPosts.forEach { post ->
            post.location?.let { location ->
                Marker(
                    state = MarkerState(
                        position = LatLng(location.latitude, location.longitude)
                    ),
                    title = post.user.displayName,
                    snippet = post.caption.take(50),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                    onClick = {
                        onEvent(MapsUiEvent.MarkerClick(post.id))
                        true
                    }
                )
            }
        }
    }

    // Loading overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PostDetailBubble(
    post: Post,
    onDismiss: () -> Unit,
    onLoveClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onDismiss() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // User info
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.user.profileImageUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.user.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${post.user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Love button
            IconButton(onClick = onLoveClick) {
                Icon(
                    imageVector = if (post.isLoved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (post.isLoved) "Unlike" else "Like",
                    tint = if (post.isLoved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Post image preview
        post.mediaItems.firstOrNull()?.let { media ->
            AsyncImage(
                model = media.thumbnailUrl ?: media.url,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Caption
        if (post.caption.isNotBlank()) {
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Location
        post.location?.let { location ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = location.name ?: location.address ?: "Unknown location",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${post.lovesCount} loves",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${post.commentsCount} comments",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocationPermissionRequiredView(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Location Permission Required",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "To show nearby posts on the map,\nwe need access to your location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun LoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Getting your location...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Preview helpers
private fun createMockUser() = User(
    id = "1",
    username = "cat_lover",
    displayName = "Cat Lover",
    bio = "I love cats!",
    profileImageUrl = "https://picsum.photos/200?random=1",
    followersCount = 1234,
    followingCount = 567,
    postsCount = 89,
    isFollowing = false,
    createdAt = System.currentTimeMillis()
)

private fun createMockPost(id: String = "1", isLoved: Boolean = false) = Post(
    id = id,
    userId = "1",
    user = createMockUser(),
    caption = "Found this adorable cat sleeping in the sun! 🐱☀️",
    mediaItems = listOf(
        MediaItem.Image(
            url = "https://picsum.photos/800/600?random=10",
            thumbnailUrl = "https://picsum.photos/200/150?random=10",
            width = 800,
            height = 600
        )
    ),
    location = Location(
        latitude = -6.2088,
        longitude = 106.8456,
        address = "Jakarta, Indonesia",
        name = "Jakarta"
    ),
    lovesCount = 234,
    commentsCount = 45,
    repliesCount = 12,
    isLoved = isLoved,
    createdAt = System.currentTimeMillis()
)

// Previews
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MapsContentPermissionRequiredPreview() {
    MeetCatTheme {
        MapsContent(
            uiState = MapsUiState(
                hasLocationPermission = false
            ),
            onEvent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MapsContentLoadingPreview() {
    MeetCatTheme {
        MapsContent(
            uiState = MapsUiState(
                hasLocationPermission = true,
                isLoadingLocation = true
            ),
            onEvent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MapsContentErrorPreview() {
    MeetCatTheme {
        MapsContent(
            uiState = MapsUiState(
                hasLocationPermission = true,
                error = "Failed to get location"
            ),
            onEvent = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostDetailBubblePreview() {
    MeetCatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            PostDetailBubble(
                post = createMockPost(),
                onDismiss = {},
                onLoveClick = {},
                onProfileClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostDetailBubbleLovedPreview() {
    MeetCatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            PostDetailBubble(
                post = createMockPost(isLoved = true),
                onDismiss = {},
                onLoveClick = {},
                onProfileClick = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LocationPermissionRequiredViewPreview() {
    MeetCatTheme {
        LocationPermissionRequiredView(
            onRequestPermission = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoadingViewPreview() {
    MeetCatTheme {
        LoadingView()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ErrorViewPreview() {
    MeetCatTheme {
        ErrorView(
            message = "Failed to get location",
            onRetry = {}
        )
    }
}
