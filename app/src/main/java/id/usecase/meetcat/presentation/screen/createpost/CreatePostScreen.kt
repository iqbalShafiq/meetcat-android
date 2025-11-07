package id.usecase.meetcat.presentation.screen.createpost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is CreatePostUiEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is CreatePostUiEffect.ShowMediaPicker -> {
                    // Navigate to camera screen
                    onNavigateToCamera?.invoke()
                }

                is CreatePostUiEffect.ShowLocationPicker -> {
                    // Mock: In production, this would either:
                    // 1. Navigate to a location picker screen with map
                    // 2. Show a bottom sheet with nearby locations
                    // 3. Use Google Places API to search locations
                    // Would require location permissions and Google Maps SDK
                }

                is CreatePostUiEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }

                is CreatePostUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    CreatePostContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CreatePostContent(
    uiState: CreatePostUiState,
    onEvent: (CreatePostUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Post",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(CreatePostUiEvent.NavigateBack) },
                        enabled = !uiState.isUploading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            FlexibleBottomAppBar(
                scrollBehavior = scrollBehavior,
                horizontalArrangement = Arrangement.SpaceEvenly,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                // Add Photo Button
                IconButton(
                    onClick = { onEvent(CreatePostUiEvent.SelectMedia) },
                    enabled = !uiState.isUploading && uiState.mediaUri == null
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Add Photo"
                    )
                }

                // Add Location Button
                IconButton(
                    onClick = { onEvent(CreatePostUiEvent.SelectLocation) },
                    enabled = !uiState.isUploading && uiState.locationAddress == null
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Add Location"
                    )
                }

                // Post Button (Primary)
                FilledIconButton(
                    onClick = { onEvent(CreatePostUiEvent.CreatePost) },
                    enabled = !uiState.isUploading,
                    modifier = Modifier.width(56.dp)
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Post"
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Upload progress
            if (uiState.isUploading) {
                LinearProgressIndicator(
                    progress = { uiState.uploadProgress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                    // Caption TextField
                    TextField(
                        value = uiState.caption,
                        onValueChange = { onEvent(CreatePostUiEvent.CaptionChanged(it)) },
                        placeholder = { Text("What's on your mind about cats?") },
                        isError = uiState.captionError != null,
                        supportingText = {
                            if (uiState.captionError != null) {
                                Text(uiState.captionError)
                            } else {
                                Text("${uiState.caption.length}/500")
                            }
                        },
                        enabled = !uiState.isUploading,
                        minLines = 5,
                        maxLines = 10,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Media Preview
                    if (uiState.mediaUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            coil3.compose.AsyncImage(
                                model = uiState.mediaUri,
                                contentDescription = "Selected media",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            // Remove button
                            IconButton(
                                onClick = { onEvent(CreatePostUiEvent.RemoveMedia) },
                                enabled = !uiState.isUploading,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove media",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Location Preview
                    if (uiState.locationAddress != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.locationAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onEvent(CreatePostUiEvent.RemoveLocation) },
                                enabled = !uiState.isUploading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove location",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

@Preview(showBackground = true)
@Composable
private fun CreatePostScreenPreview() {
    MeetCatTheme {
        CreatePostContent(
            uiState = CreatePostUiState(
                caption = "Check out my cute cat!",
                locationAddress = "Jakarta, Indonesia"
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreatePostScreenUploadingPreview() {
    MeetCatTheme {
        CreatePostContent(
            uiState = CreatePostUiState(
                caption = "Check out my cute cat!",
                isUploading = true,
                uploadProgress = 0.5f
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
