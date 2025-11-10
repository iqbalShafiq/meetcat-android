package id.usecase.meetcat.presentation.screen.mediapicker

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MediaPickerScreen(
    viewModel: MediaPickerViewModel,
    onNavigateBack: () -> Unit,
    onMediaSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is MediaPickerUiEffect.NavigateBack -> {
                    onNavigateBack()
                }

                is MediaPickerUiEffect.MediaSelected -> {
                    onMediaSelected(effect.uri)
                }

                is MediaPickerUiEffect.RequestCameraPermission -> {
                    // Mock: In production, would use:
                    // val permissionLauncher = rememberLauncherForActivityResult(
                    //     contract = ActivityResultContracts.RequestPermission()
                    // ) { granted -> viewModel.onCameraPermissionResult(granted) }
                    // permissionLauncher.launch(Manifest.permission.CAMERA)
                }

                is MediaPickerUiEffect.RequestStoragePermission -> {
                    // Mock: In production, would request READ_MEDIA_IMAGES/READ_MEDIA_VIDEO
                    // on Android 13+ or READ_EXTERNAL_STORAGE on older versions
                }

                is MediaPickerUiEffect.LaunchCamera -> {
                    // Mock: In production, would use:
                    // val takePictureLauncher = rememberLauncherForActivityResult(
                    //     contract = ActivityResultContracts.TakePicture()
                    // ) { success -> if (success) viewModel.onPhotoTaken(photoUri) }
                    // takePictureLauncher.launch(photoUri)
                }

                is MediaPickerUiEffect.LaunchGallery -> {
                    // Mock: In production, would use ActivityResultContracts.PickVisualMedia
                    // or GetContent to launch system gallery picker
                }

                is MediaPickerUiEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    MediaPickerContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaPickerContent(
    uiState: MediaPickerUiState,
    onEvent: (MediaPickerUiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Media",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MediaPickerUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Choose how you want to add media",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camera Option
                MediaPickerOption(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    isSelected = uiState.selectedMode == MediaPickerMode.CAMERA,
                    onClick = { onEvent(MediaPickerUiEvent.ModeSelected(MediaPickerMode.CAMERA)) },
                    modifier = Modifier.weight(1f)
                )

                // Gallery Option
                MediaPickerOption(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Gallery",
                    isSelected = uiState.selectedMode == MediaPickerMode.GALLERY,
                    onClick = { onEvent(MediaPickerUiEvent.ModeSelected(MediaPickerMode.GALLERY)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Supported formats: JPG, PNG\nMax size: 10MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MediaPickerOption(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaPickerScreenPreview() {
    MeetCatTheme {
        MediaPickerContent(
            uiState = MediaPickerUiState(
                selectedMode = MediaPickerMode.GALLERY
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
