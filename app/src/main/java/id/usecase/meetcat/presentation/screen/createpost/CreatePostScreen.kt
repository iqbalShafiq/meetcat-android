package id.usecase.meetcat.presentation.screen.createpost

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                    // TODO: Show media picker
                }

                is CreatePostUiEffect.ShowLocationPicker -> {
                    // TODO: Show location picker
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostContent(
    uiState: CreatePostUiState,
    onEvent: (CreatePostUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                actions = {
                    TextButton(
                        onClick = { onEvent(CreatePostUiEvent.CreatePost) },
                        enabled = !uiState.isUploading
                    ) {
                        if (uiState.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post")
                        }
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
                    .weight(1f)
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
                        // TODO: Add AsyncImage to display media
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Remove button
                        IconButton(
                            onClick = { onEvent(CreatePostUiEvent.RemoveMedia) },
                            enabled = !uiState.isUploading,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove media",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEvent(CreatePostUiEvent.SelectMedia) },
                        enabled = !uiState.isUploading && uiState.mediaUri == null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Photo")
                    }

                    OutlinedButton(
                        onClick = { onEvent(CreatePostUiEvent.SelectLocation) },
                        enabled = !uiState.isUploading && uiState.locationAddress == null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Location")
                    }
                }
            }

            // Create Button at bottom
            Button(
                onClick = { onEvent(CreatePostUiEvent.CreatePost) },
                enabled = !uiState.isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp)
            ) {
                if (uiState.isUploading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                    )
                    Text("Posting...")
                } else {
                    Text("Post", style = MaterialTheme.typography.labelLarge)
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
