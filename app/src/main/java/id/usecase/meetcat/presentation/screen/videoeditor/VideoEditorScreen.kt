package id.usecase.meetcat.presentation.screen.videoeditor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Main VideoEditor Screen
 * Provides a video editing interface with timeline, preview, and layer controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: VideoEditorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    // Handle UI Effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is VideoEditorUiEffect.NavigateBack -> onNavigateBack()
                is VideoEditorUiEffect.OpenBackgroundPicker -> {
                    scope.launch { scaffoldState.bottomSheetState.expand() }
                }
                is VideoEditorUiEffect.OpenObjectPicker -> {
                    scope.launch { scaffoldState.bottomSheetState.expand() }
                }
                is VideoEditorUiEffect.OpenAudioPicker -> {
                    scope.launch { scaffoldState.bottomSheetState.expand() }
                }
                is VideoEditorUiEffect.ShowError -> {
                    // TODO: Show snackbar or toast
                }
                is VideoEditorUiEffect.ExportSuccess -> {
                    // TODO: Show success message and share options
                }
            }
        }
    }

    var selectedTab by remember { mutableStateOf(PickerTab.BACKGROUND) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Video Editor") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(VideoEditorUiEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.onEvent(VideoEditorUiEvent.OnExportClicked) },
                        enabled = !uiState.isExporting && uiState.backgroundLayers.isNotEmpty()
                    ) {
                        Text("Export")
                    }
                }
            )
        },
        sheetContent = {
            BottomSheetContent(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        },
        sheetPeekHeight = 280.dp
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Preview Area
            PreviewSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                uiState = uiState
            )

            // Export Progress
            AnimatedVisibility(visible = uiState.isExporting) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Exporting video...")
                    LinearProgressIndicator(
                        progress = { uiState.exportProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Playback Controls
            PlaybackControls(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
                onEvent = viewModel::onEvent
            )

            // Timeline
            TimelineSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }
    }
}

/**
 * Preview section showing the composed video
 */
@Composable
private fun PreviewSection(
    modifier: Modifier = Modifier,
    uiState: VideoEditorUiState
) {
    Box(
        modifier = modifier
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Preview placeholder - in real implementation, use ExoPlayer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Preview Area\n${uiState.backgroundLayers.size} backgrounds, ${uiState.objectLayers.size} objects",
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Playback controls (play/pause, seek bar)
 */
@Composable
private fun PlaybackControls(
    modifier: Modifier = Modifier,
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Seek bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(uiState.currentPositionMs),
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = uiState.currentPositionMs.toFloat(),
                onValueChange = { onEvent(VideoEditorUiEvent.OnSeekTo(it.toLong())) },
                valueRange = 0f..uiState.totalDurationMs.toFloat(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
            Text(
                text = formatTime(uiState.totalDurationMs),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Play/Pause button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onEvent(VideoEditorUiEvent.OnPlayPauseClicked) }
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * Timeline section showing all layers
 */
@Composable
private fun TimelineSection(
    modifier: Modifier = Modifier,
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Background track
        TimelineTrack(
            label = "Background",
            items = uiState.backgroundLayers,
            totalDuration = uiState.totalDurationMs,
            currentPosition = uiState.currentPositionMs,
            color = MaterialTheme.colorScheme.primary,
            onItemRemove = { onEvent(VideoEditorUiEvent.OnBackgroundRemoved(it)) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Object track
        TimelineTrack(
            label = "Objects",
            items = uiState.objectLayers,
            totalDuration = uiState.totalDurationMs,
            currentPosition = uiState.currentPositionMs,
            color = MaterialTheme.colorScheme.secondary,
            onItemRemove = { onEvent(VideoEditorUiEvent.OnObjectRemoved(it)) }
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Audio track
        TimelineTrack(
            label = "Audio",
            items = uiState.audioTracks,
            totalDuration = uiState.totalDurationMs,
            currentPosition = uiState.currentPositionMs,
            color = MaterialTheme.colorScheme.tertiary,
            onItemRemove = { onEvent(VideoEditorUiEvent.OnAudioRemoved(it)) }
        )
    }
}

/**
 * Generic timeline track for displaying items
 */
@Composable
private fun <T : Any> TimelineTrack(
    label: String,
    items: List<T>,
    totalDuration: Long,
    currentPosition: Long,
    color: Color,
    onItemRemove: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
        ) {
            // Draw timeline items
            Canvas(modifier = Modifier.fillMaxSize()) {
                items.forEach { item ->
                    val (startMs, endMs, id) = when (item) {
                        is BackgroundLayer -> Triple(item.startMs, item.endMs, item.id)
                        is ObjectLayer -> Triple(item.startMs, item.endMs, item.id)
                        is AudioTrack -> Triple(item.startMs, item.endMs, item.id)
                        else -> return@forEach
                    }

                    val startX = (startMs.toFloat() / totalDuration) * size.width
                    val width = ((endMs - startMs).toFloat() / totalDuration) * size.width

                    drawRect(
                        color = color,
                        topLeft = Offset(startX, 0f),
                        size = Size(width, size.height)
                    )
                }

                // Draw playhead
                val playheadX = (currentPosition.toFloat() / totalDuration) * size.width
                drawLine(
                    color = Color.Red,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

/**
 * Bottom sheet content for selecting backgrounds, objects, and audio
 */
@Composable
private fun BottomSheetContent(
    selectedTab: PickerTab,
    onTabSelected: (PickerTab) -> Unit,
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp)
    ) {
        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PickerTab.entries.forEach { tab ->
                FilledTonalButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = when (tab) {
                            PickerTab.BACKGROUND -> Icons.Default.Image
                            PickerTab.OBJECTS -> Icons.Default.Pets
                            PickerTab.AUDIO -> Icons.Default.AudioFile
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tab.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTab) {
            PickerTab.BACKGROUND -> BackgroundPicker(onEvent)
            PickerTab.OBJECTS -> ObjectPicker(uiState.availableObjects, onEvent)
            PickerTab.AUDIO -> AudioPicker(onEvent)
        }
    }
}

/**
 * Background picker with gallery access
 */
@Composable
private fun BackgroundPicker(onEvent: (VideoEditorUiEvent) -> Unit) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Add background with default 5-second duration at current position
            onEvent(VideoEditorUiEvent.OnBackgroundSelected(it, 0, 5000))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { galleryLauncher.launch("image/*") }
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose from Gallery")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select a background image or video",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Object picker showing available cat GIFs/videos
 */
@Composable
private fun ObjectPicker(
    objects: List<CatObject>,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    if (objects.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(objects) { catObject ->
                CatObjectItem(
                    catObject = catObject,
                    onClick = {
                        // Add object with default 3-second duration at current position
                        onEvent(VideoEditorUiEvent.OnObjectSelected(catObject.id, 0, 3000))
                    }
                )
            }
        }
    }
}

/**
 * Individual cat object item
 */
@Composable
private fun CatObjectItem(
    catObject: CatObject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = catObject.thumbnailUri,
                contentDescription = catObject.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = catObject.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Audio picker with file access
 */
@Composable
private fun AudioPicker(onEvent: (VideoEditorUiEvent) -> Unit) {
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Add audio with default 10-second duration at current position
            onEvent(VideoEditorUiEvent.OnAudioSelected(it, 0, 10000))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { audioLauncher.launch("audio/*") }
        ) {
            Icon(Icons.Default.AudioFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Audio File")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select an audio track",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Helper function to format milliseconds to MM:SS
 */
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Picker tabs
 */
private enum class PickerTab {
    BACKGROUND,
    OBJECTS,
    AUDIO
}
