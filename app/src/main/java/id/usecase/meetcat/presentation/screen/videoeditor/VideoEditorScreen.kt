package id.usecase.meetcat.presentation.screen.videoeditor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import coil3.compose.AsyncImage
import id.usecase.meetcat.presentation.component.state.LoadingView
import id.usecase.meetcat.presentation.component.videoeditor.ExportProgressDialog
import id.usecase.meetcat.presentation.component.videoeditor.ObjectSoundEditDialog
import id.usecase.meetcat.presentation.component.videoeditor.TimelineItem
import id.usecase.meetcat.presentation.component.videoeditor.TimelineTrack
import id.usecase.meetcat.presentation.screen.videoeditor.BackgroundAsset
import id.usecase.meetcat.presentation.screen.videoeditor.BackgroundType
import id.usecase.meetcat.presentation.screen.videoeditor.SoundAsset
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * 🎬 MeetCat Video Editor Screen
 * Create adorable cat videos with backgrounds, objects, and sounds!
 */
@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
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
    var showExportSuccess by remember { mutableStateOf(false) }

    // Handle UI Effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is VideoEditorUiEffect.NavigateBack -> onNavigateBack()
                is VideoEditorUiEffect.OpenBackgroundPicker,
                is VideoEditorUiEffect.OpenObjectPicker,
                is VideoEditorUiEffect.OpenAudioPicker -> {
                    scope.launch { scaffoldState.bottomSheetState.expand() }
                }
                is VideoEditorUiEffect.ShowError -> {
                    // TODO: Show error snackbar
                }
                is VideoEditorUiEffect.ExportSuccess -> {
                    showExportSuccess = true
                }
            }
        }
    }

    var selectedTab by remember { mutableStateOf(PickerTab.BACKGROUND) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cat Video Studio")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(VideoEditorUiEvent.OnBackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        sheetContent = {
            AssetPickerSheet(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        },
        sheetPeekHeight = 72.dp // Only show tabs when collapsed
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Preview Section - Bigger!
                PreviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    uiState = uiState
                )

                // Timeline at the bottom
                TimelineSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )

                // Playback Controls at very bottom
                PlaybackControls(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }

            // Export FAB
            AnimatedVisibility(
                visible = !uiState.isExporting && uiState.backgroundLayers.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.onEvent(VideoEditorUiEvent.OnExportClicked) },
                    icon = {
                        Icon(Icons.Default.Pets, contentDescription = null)
                    },
                    text = { Text("🎬 Export Video") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }

    // Export Progress Dialog
    if (uiState.isExporting) {
        ExportProgressDialog(
            progress = uiState.exportProgress
        )
    }

    // Object Sound Edit Dialog
    uiState.selectedObjectForSoundEdit?.let { objectId ->
        val selectedObject = uiState.objectLayers.find { it.id == objectId }
        selectedObject?.let { obj ->
            val catObject = uiState.availableObjects.find { it.id == obj.objectId }
            ObjectSoundEditDialog(
                objectName = catObject?.name ?: "Cat Object",
                objectDurationMs = obj.endMs - obj.startMs,
                currentSoundConfig = obj.soundConfig,
                availableSounds = uiState.availableSounds.toList(),
                onDismiss = {
                    viewModel.onEvent(VideoEditorUiEvent.OnObjectSoundUpdated(objectId, obj.soundConfig))
                },
                onSave = { soundConfig ->
                    viewModel.onEvent(VideoEditorUiEvent.OnObjectSoundUpdated(objectId, soundConfig))
                }
            )
        }
    }
}

/**
 * Preview section showing composed video
 */
@Composable
private fun PreviewSection(
    modifier: Modifier = Modifier,
    uiState: VideoEditorUiState
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.backgroundLayers.isEmpty()) {
            // Empty state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Start by adding a background! 🎨",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Then add cute cats and sounds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // TODO: Show ExoPlayer preview with composed video
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Preview: ${uiState.backgroundLayers.size} bg, ${uiState.objectLayers.size} cats, ${uiState.audioTracks.size} sounds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Playback controls
 */
@Composable
private fun PlaybackControls(
    modifier: Modifier = Modifier,
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Time slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(uiState.currentPositionMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = uiState.currentPositionMs.toFloat(),
                onValueChange = { onEvent(VideoEditorUiEvent.OnSeekTo(it.toLong())) },
                valueRange = 0f..uiState.totalDurationMs.toFloat(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Text(
                text = formatTime(uiState.totalDurationMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Play button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onEvent(VideoEditorUiEvent.OnPlayPauseClicked) },
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Timeline section with all tracks
 */
@Composable
private fun TimelineSection(
    modifier: Modifier = Modifier,
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Background track
        TimelineTrack(
            label = "🎨 Backgrounds",
            items = uiState.backgroundLayers.map {
                TimelineItem(it.id, it.startMs, it.endMs)
            },
            totalDurationMs = uiState.totalDurationMs,
            currentPositionMs = uiState.currentPositionMs,
            color = MaterialTheme.colorScheme.primary
        )

        // Objects track (with sound indicators)
        TimelineTrack(
            label = "🐱 Cat Objects (tap to add sound)",
            items = uiState.objectLayers.map {
                TimelineItem(
                    id = it.id,
                    startMs = it.startMs,
                    endMs = it.endMs,
                    hasSound = it.soundConfig != null
                )
            },
            totalDurationMs = uiState.totalDurationMs,
            currentPositionMs = uiState.currentPositionMs,
            color = MaterialTheme.colorScheme.secondary,
            showSoundIndicator = true,
            onItemClick = { objectId ->
                onEvent(VideoEditorUiEvent.OnObjectClicked(objectId))
            }
        )

        // Audio track
        TimelineTrack(
            label = "🎵 Sounds",
            items = uiState.audioTracks.map {
                TimelineItem(it.id, it.startMs, it.endMs)
            },
            totalDurationMs = uiState.totalDurationMs,
            currentPositionMs = uiState.currentPositionMs,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

/**
 * Asset picker bottom sheet
 */
@Composable
private fun AssetPickerSheet(
    selectedTab: PickerTab,
    onTabSelected: (PickerTab) -> Unit,
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Drag handle indicator
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Icon-only Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PickerTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                IconButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = when (tab) {
                            PickerTab.BACKGROUND -> Icons.Default.Image
                            PickerTab.OBJECTS -> Icons.Default.Pets
                            PickerTab.AUDIO -> Icons.Default.AudioFile
                        },
                        contentDescription = tab.label,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content area with minimum height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            when (selectedTab) {
                PickerTab.BACKGROUND -> BackgroundPicker(
                    backgrounds = uiState.availableBackgrounds,
                    onEvent = onEvent
                )
                PickerTab.OBJECTS -> ObjectPicker(
                    objects = uiState.availableObjects,
                    onEvent = onEvent
                )
                PickerTab.AUDIO -> AudioPicker(
                    sounds = uiState.availableSounds,
                    onEvent = onEvent
                )
            }
        }
    }
}

/**
 * Background picker
 */
@Composable
private fun BackgroundPicker(
    backgrounds: List<BackgroundAsset>,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onEvent(VideoEditorUiEvent.OnBackgroundSelected(it, 0, 5000))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (backgrounds.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(backgrounds) { bg ->
                    AssetCard(
                        imageUri = bg.thumbnailUri,
                        label = bg.name,
                        onClick = {
                            onEvent(VideoEditorUiEvent.OnBackgroundSelected(bg.uri, 0, 5000))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose from Gallery")
        }
    }
}

/**
 * Object picker
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
            Text(
                text = "No cat objects available.\nAdd GIFs to assets/cats/ folder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(objects) { cat ->
                AssetCard(
                    imageUri = cat.thumbnailUri.toUri(),
                    label = cat.name,
                    onClick = {
                        onEvent(VideoEditorUiEvent.OnObjectSelected(cat.id, 0, 3000))
                    }
                )
            }
        }
    }
}

/**
 * Audio picker
 */
@Composable
private fun AudioPicker(
    sounds: List<SoundAsset>,
    onEvent: (VideoEditorUiEvent) -> Unit
) {
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onEvent(VideoEditorUiEvent.OnAudioSelected(it, 0, 10000))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (sounds.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sounds) { sound ->
                    SoundItem(
                        sound = sound,
                        onClick = {
                            onEvent(VideoEditorUiEvent.OnAudioSelected(sound.uri, 0, 10000))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { audioLauncher.launch("audio/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AudioFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Audio File")
        }
    }
}

/**
 * Asset card component
 */
@Composable
private fun AssetCard(
    imageUri: Uri,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = label,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Sound item component
 */
@Composable
private fun SoundItem(
    sound: SoundAsset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = sound.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Format milliseconds to MM:SS
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
private enum class PickerTab(val label: String) {
    BACKGROUND("Backgrounds"),
    OBJECTS("Cats"),
    AUDIO("Sounds")
}

// ================================
// Previews
// ================================

/**
 * Preview: Empty state - no content added yet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VideoEditorScreenEmptyPreview() {
    MeetCatTheme {
        VideoEditorScreenContent(
            uiState = VideoEditorUiState(
                backgroundLayers = persistentListOf(),
                objectLayers = persistentListOf(),
                audioTracks = persistentListOf(),
                availableObjects = persistentListOf(),
                availableBackgrounds = persistentListOf(),
                availableSounds = persistentListOf(),
                currentPositionMs = 0,
                totalDurationMs = 30000,
                isPlaying = false,
                isExporting = false,
                exportProgress = 0f
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

/**
 * Preview: With content - backgrounds, objects, and audio added
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VideoEditorScreenWithContentPreview() {
    MeetCatTheme {
        VideoEditorScreenContent(
            uiState = VideoEditorUiState(
                backgroundLayers = listOf(
                    BackgroundLayer(
                        id = "bg1",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg1.jpg"),
                        startMs = 0,
                        endMs = 10000
                    ),
                    BackgroundLayer(
                        id = "bg2",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg2.jpg"),
                        startMs = 10000,
                        endMs = 20000
                    )
                ).toImmutableList(),
                objectLayers = listOf(
                    ObjectLayer(
                        id = "obj1",
                        objectId = "cat1",
                        startMs = 2000,
                        endMs = 8000,
                        soundConfig = ObjectSoundConfig(
                            soundUri = Uri.parse("file:///android_asset/sounds/meow.mp3"),
                            soundName = "Meow",
                            offsetMs = 0,
                            durationMs = null,
                            volume = 1f
                        )
                    ),
                    ObjectLayer(
                        id = "obj2",
                        objectId = "cat2",
                        startMs = 12000,
                        endMs = 18000
                    )
                ).toImmutableList(),
                audioTracks = listOf(
                    AudioTrack(
                        id = "audio1",
                        uri = Uri.parse("file:///android_asset/sounds/background.mp3"),
                        startMs = 0,
                        endMs = 20000,
                        name = "Background Music"
                    )
                ).toImmutableList(),
                availableObjects = listOf(
                    CatObject(
                        id = "cat1",
                        name = "Dancing Cat",
                        thumbnailUri = "file:///android_asset/cats/cat1.gif",
                        resourceUri = "file:///android_asset/cats/cat1.gif",
                        type = CatObjectType.GIF,
                        defaultSoundUri = Uri.parse("file:///android_asset/sounds/meow.mp3"),
                        defaultSoundName = "Meow"
                    ),
                    CatObject(
                        id = "cat2",
                        name = "Cute Kitten",
                        thumbnailUri = "file:///android_asset/cats/cat2.gif",
                        resourceUri = "file:///android_asset/cats/cat2.gif",
                        type = CatObjectType.GIF
                    )
                ).toImmutableList(),
                availableBackgrounds = listOf(
                    BackgroundAsset(
                        id = "bg1",
                        name = "Beach",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg1.jpg"),
                        thumbnailUri = Uri.parse("file:///android_asset/backgrounds/bg1.jpg"),
                        type = BackgroundType.IMAGE
                    ),
                    BackgroundAsset(
                        id = "bg2",
                        name = "Garden",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg2.jpg"),
                        thumbnailUri = Uri.parse("file:///android_asset/backgrounds/bg2.jpg"),
                        type = BackgroundType.IMAGE
                    )
                ).toImmutableList(),
                availableSounds = listOf(
                    SoundAsset(
                        id = "sound1",
                        name = "Meow Sound",
                        uri = Uri.parse("file:///android_asset/sounds/meow.mp3"),
                        duration = 3000L
                    ),
                    SoundAsset(
                        id = "sound2",
                        name = "Purr Sound",
                        uri = Uri.parse("file:///android_asset/sounds/purr.mp3"),
                        duration = 5000L
                    )
                ).toImmutableList(),
                currentPositionMs = 5000,
                totalDurationMs = 30000,
                isPlaying = false,
                isExporting = false,
                exportProgress = 0f
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

/**
 * Preview: Exporting state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VideoEditorScreenExportingPreview() {
    MeetCatTheme {
        VideoEditorScreenContent(
            uiState = VideoEditorUiState(
                backgroundLayers = listOf(
                    BackgroundLayer(
                        id = "bg1",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg1.jpg"),
                        startMs = 0,
                        endMs = 10000
                    )
                ).toImmutableList(),
                objectLayers = listOf(
                    ObjectLayer(
                        id = "obj1",
                        objectId = "cat1",
                        startMs = 2000,
                        endMs = 8000
                    )
                ).toImmutableList(),
                audioTracks = persistentListOf(),
                availableObjects = persistentListOf(),
                availableBackgrounds = persistentListOf(),
                availableSounds = persistentListOf(),
                currentPositionMs = 0,
                totalDurationMs = 30000,
                isPlaying = false,
                isExporting = true,
                exportProgress = 0.65f
            ),
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

/**
 * Extracted content composable for previews
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoEditorScreenContent(
    uiState: VideoEditorUiState,
    onEvent: (VideoEditorUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    var selectedTab by remember { mutableStateOf(PickerTab.BACKGROUND) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cat Video Studio")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        sheetContent = {
            AssetPickerSheet(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                uiState = uiState,
                onEvent = onEvent
            )
        },
        sheetPeekHeight = 72.dp // Only show tabs when collapsed
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Preview Section - Bigger!
                PreviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    uiState = uiState
                )

                // Timeline at the bottom
                TimelineSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    uiState = uiState,
                    onEvent = onEvent
                )

                // Playback Controls at very bottom
                PlaybackControls(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

            // Export FAB
            AnimatedVisibility(
                visible = !uiState.isExporting && uiState.backgroundLayers.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { onEvent(VideoEditorUiEvent.OnExportClicked) },
                    icon = {
                        Icon(Icons.Default.Pets, contentDescription = null)
                    },
                    text = { Text("🎬 Export Video") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }

    // Export Progress Dialog
    if (uiState.isExporting) {
        ExportProgressDialog(
            progress = uiState.exportProgress
        )
    }
}

/**
 * Preview: Preview section only - empty state
 */
@Preview(showBackground = true)
@Composable
private fun PreviewSectionEmptyPreview() {
    MeetCatTheme {
        PreviewSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            uiState = VideoEditorUiState()
        )
    }
}

/**
 * Preview: Preview section only - with content
 */
@Preview(showBackground = true)
@Composable
private fun PreviewSectionWithContentPreview() {
    MeetCatTheme {
        PreviewSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            uiState = VideoEditorUiState(
                backgroundLayers = listOf(
                    BackgroundLayer(
                        id = "bg1",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg1.jpg"),
                        startMs = 0,
                        endMs = 10000
                    )
                ).toImmutableList(),
                objectLayers = listOf(
                    ObjectLayer(
                        id = "obj1",
                        objectId = "cat1",
                        startMs = 2000,
                        endMs = 8000
                    )
                ).toImmutableList(),
                audioTracks = listOf(
                    AudioTrack(
                        id = "audio1",
                        uri = Uri.parse("file:///android_asset/sounds/bg.mp3"),
                        startMs = 0,
                        endMs = 10000,
                        name = "Background Music"
                    )
                ).toImmutableList()
            )
        )
    }
}

/**
 * Preview: Timeline section
 */
@Preview(showBackground = true)
@Composable
private fun TimelineSectionPreview() {
    MeetCatTheme {
        TimelineSection(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            uiState = VideoEditorUiState(
                backgroundLayers = listOf(
                    BackgroundLayer(
                        id = "bg1",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg1.jpg"),
                        startMs = 0,
                        endMs = 10000
                    ),
                    BackgroundLayer(
                        id = "bg2",
                        uri = Uri.parse("file:///android_asset/backgrounds/bg2.jpg"),
                        startMs = 10000,
                        endMs = 20000
                    )
                ).toImmutableList(),
                objectLayers = listOf(
                    ObjectLayer(
                        id = "obj1",
                        objectId = "cat1",
                        startMs = 2000,
                        endMs = 8000,
                        soundConfig = ObjectSoundConfig(
                            soundUri = Uri.parse("file:///android_asset/sounds/meow.mp3"),
                            soundName = "Meow",
                            offsetMs = 0,
                            durationMs = null,
                            volume = 1f
                        )
                    ),
                    ObjectLayer(
                        id = "obj2",
                        objectId = "cat2",
                        startMs = 12000,
                        endMs = 18000
                    )
                ).toImmutableList(),
                audioTracks = listOf(
                    AudioTrack(
                        id = "audio1",
                        uri = Uri.parse("file:///android_asset/sounds/bg.mp3"),
                        startMs = 0,
                        endMs = 20000,
                        name = "Background Music"
                    )
                ).toImmutableList(),
                currentPositionMs = 5000,
                totalDurationMs = 30000
            ),
            onEvent = {}
        )
    }
}

/**
 * Preview: Asset picker sheet
 */
@Preview(showBackground = true)
@Composable
private fun AssetPickerSheetPreview() {
    MeetCatTheme {
        AssetPickerSheet(
            selectedTab = PickerTab.OBJECTS,
            onTabSelected = {},
            uiState = VideoEditorUiState(
                availableObjects = listOf(
                    CatObject(
                        id = "cat1",
                        name = "Dancing Cat",
                        thumbnailUri = "file:///android_asset/cats/cat1.gif",
                        resourceUri = "file:///android_asset/cats/cat1.gif",
                        type = CatObjectType.GIF,
                        defaultSoundUri = Uri.parse("file:///android_asset/sounds/meow.mp3"),
                        defaultSoundName = "Meow"
                    ),
                    CatObject(
                        id = "cat2",
                        name = "Cute Kitten",
                        thumbnailUri = "file:///android_asset/cats/cat2.gif",
                        resourceUri = "file:///android_asset/cats/cat2.gif",
                        type = CatObjectType.GIF
                    ),
                    CatObject(
                        id = "cat3",
                        name = "Sleepy Cat",
                        thumbnailUri = "file:///android_asset/cats/cat3.gif",
                        resourceUri = "file:///android_asset/cats/cat3.gif",
                        type = CatObjectType.GIF
                    )
                ).toImmutableList()
            ),
            onEvent = {}
        )
    }
}
