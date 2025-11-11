package id.usecase.meetcat.presentation.screen.videoeditor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for VideoEditor screen
 * Manages the state of backgrounds, objects, and audio tracks in the timeline
 */
class VideoEditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VideoEditorUiState())
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<VideoEditorUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadAvailableCatObjects()
    }

    fun onEvent(event: VideoEditorUiEvent) {
        when (event) {
            // Background Events
            is VideoEditorUiEvent.OnBackgroundSelected -> handleBackgroundSelected(event)
            is VideoEditorUiEvent.OnBackgroundRemoved -> handleBackgroundRemoved(event)

            // Object Events
            is VideoEditorUiEvent.OnObjectSelected -> handleObjectSelected(event)
            is VideoEditorUiEvent.OnObjectMoved -> handleObjectMoved(event)
            is VideoEditorUiEvent.OnObjectRemoved -> handleObjectRemoved(event)

            // Audio Events
            is VideoEditorUiEvent.OnAudioSelected -> handleAudioSelected(event)
            is VideoEditorUiEvent.OnAudioRemoved -> handleAudioRemoved(event)
            is VideoEditorUiEvent.OnAudioMoved -> handleAudioMoved(event)

            // Playback Events
            is VideoEditorUiEvent.OnPlayPauseClicked -> handlePlayPause()
            is VideoEditorUiEvent.OnSeekTo -> handleSeekTo(event)

            // Export Events
            is VideoEditorUiEvent.OnExportClicked -> handleExport()
            is VideoEditorUiEvent.OnExportCompleted -> handleExportCompleted()

            // UI Events
            is VideoEditorUiEvent.OnTimelineScaleChanged -> handleTimelineScaleChanged(event)
            is VideoEditorUiEvent.OnBackClicked -> handleBackClicked()
            is VideoEditorUiEvent.OnOpenBackgroundPicker -> handleOpenBackgroundPicker()
            is VideoEditorUiEvent.OnOpenObjectPicker -> handleOpenObjectPicker()
            is VideoEditorUiEvent.OnOpenAudioPicker -> handleOpenAudioPicker()
        }
    }

    // Background Handlers
    private fun handleBackgroundSelected(event: VideoEditorUiEvent.OnBackgroundSelected) {
        val newBackground = BackgroundLayer(
            id = UUID.randomUUID().toString(),
            uri = event.uri,
            startMs = event.startMs,
            endMs = event.endMs
        )

        _uiState.update { state ->
            state.copy(
                backgroundLayers = (state.backgroundLayers + newBackground).toPersistentList()
            )
        }
    }

    private fun handleBackgroundRemoved(event: VideoEditorUiEvent.OnBackgroundRemoved) {
        _uiState.update { state ->
            state.copy(
                backgroundLayers = state.backgroundLayers
                    .filter { it.id != event.id }
                    .toPersistentList()
            )
        }
    }

    // Object Handlers
    private fun handleObjectSelected(event: VideoEditorUiEvent.OnObjectSelected) {
        val newObject = ObjectLayer(
            id = UUID.randomUUID().toString(),
            objectId = event.objectId,
            startMs = event.startMs,
            endMs = event.endMs
        )

        _uiState.update { state ->
            state.copy(
                objectLayers = (state.objectLayers + newObject).toPersistentList()
            )
        }
    }

    private fun handleObjectMoved(event: VideoEditorUiEvent.OnObjectMoved) {
        _uiState.update { state ->
            state.copy(
                objectLayers = state.objectLayers
                    .map { layer ->
                        if (layer.id == event.id) {
                            layer.copy(
                                startMs = event.newStartMs,
                                endMs = event.newEndMs
                            )
                        } else {
                            layer
                        }
                    }
                    .toPersistentList()
            )
        }
    }

    private fun handleObjectRemoved(event: VideoEditorUiEvent.OnObjectRemoved) {
        _uiState.update { state ->
            state.copy(
                objectLayers = state.objectLayers
                    .filter { it.id != event.id }
                    .toPersistentList()
            )
        }
    }

    // Audio Handlers
    private fun handleAudioSelected(event: VideoEditorUiEvent.OnAudioSelected) {
        val newAudio = AudioTrack(
            id = UUID.randomUUID().toString(),
            uri = event.uri,
            startMs = event.startMs,
            endMs = event.endMs,
            name = "Audio ${_uiState.value.audioTracks.size + 1}"
        )

        _uiState.update { state ->
            state.copy(
                audioTracks = (state.audioTracks + newAudio).toPersistentList()
            )
        }
    }

    private fun handleAudioRemoved(event: VideoEditorUiEvent.OnAudioRemoved) {
        _uiState.update { state ->
            state.copy(
                audioTracks = state.audioTracks
                    .filter { it.id != event.id }
                    .toPersistentList()
            )
        }
    }

    private fun handleAudioMoved(event: VideoEditorUiEvent.OnAudioMoved) {
        _uiState.update { state ->
            state.copy(
                audioTracks = state.audioTracks
                    .map { track ->
                        if (track.id == event.id) {
                            track.copy(
                                startMs = event.newStartMs,
                                endMs = event.newEndMs
                            )
                        } else {
                            track
                        }
                    }
                    .toPersistentList()
            )
        }
    }

    // Playback Handlers
    private fun handlePlayPause() {
        _uiState.update { state ->
            state.copy(isPlaying = !state.isPlaying)
        }
    }

    private fun handleSeekTo(event: VideoEditorUiEvent.OnSeekTo) {
        _uiState.update { state ->
            state.copy(
                currentPositionMs = event.positionMs.coerceIn(0, state.totalDurationMs)
            )
        }
    }

    // Export Handlers
    private fun handleExport() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true, exportProgress = 0f) }

                // TODO: Implement actual Media3 Transformer export
                // This is a placeholder for the export logic
                // In real implementation, you would:
                // 1. Create EditedMediaItem for each layer
                // 2. Apply Effects (OverlayEffect, etc.)
                // 3. Use Transformer to export
                // 4. Track progress with Transformer.getProgress()

                // Simulate export progress
                for (i in 1..10) {
                    kotlinx.coroutines.delay(200)
                    _uiState.update { it.copy(exportProgress = i / 10f) }
                }

                // Mock success
                val mockOutputUri = Uri.parse("file:///mock/output.mp4")
                _uiEffect.send(VideoEditorUiEffect.ExportSuccess(mockOutputUri))

            } catch (e: Exception) {
                _uiEffect.send(VideoEditorUiEffect.ShowError(e.message ?: "Export failed"))
            } finally {
                _uiState.update { it.copy(isExporting = false, exportProgress = 0f) }
            }
        }
    }

    private fun handleExportCompleted() {
        _uiState.update { it.copy(isExporting = false, exportProgress = 0f) }
    }

    // UI Handlers
    private fun handleTimelineScaleChanged(event: VideoEditorUiEvent.OnTimelineScaleChanged) {
        _uiState.update { state ->
            state.copy(timelineScale = event.scale.coerceIn(0.5f, 3f))
        }
    }

    private fun handleBackClicked() {
        viewModelScope.launch {
            _uiEffect.send(VideoEditorUiEffect.NavigateBack)
        }
    }

    private fun handleOpenBackgroundPicker() {
        viewModelScope.launch {
            _uiEffect.send(VideoEditorUiEffect.OpenBackgroundPicker)
        }
    }

    private fun handleOpenObjectPicker() {
        viewModelScope.launch {
            _uiEffect.send(VideoEditorUiEffect.OpenObjectPicker)
        }
    }

    private fun handleOpenAudioPicker() {
        viewModelScope.launch {
            _uiEffect.send(VideoEditorUiEffect.OpenAudioPicker)
        }
    }

    // Data Loading
    private fun loadAvailableCatObjects() {
        // Mock data - in real app, load from assets or remote server
        val mockObjects = listOf(
            CatObject(
                id = "cat_1",
                name = "Dancing Cat",
                thumbnailUri = "https://placeholder.com/cat1.jpg",
                resourceUri = "asset:///cats/dancing_cat.gif",
                type = CatObjectType.GIF
            ),
            CatObject(
                id = "cat_2",
                name = "Jumping Cat",
                thumbnailUri = "https://placeholder.com/cat2.jpg",
                resourceUri = "asset:///cats/jumping_cat.gif",
                type = CatObjectType.GIF
            ),
            CatObject(
                id = "cat_3",
                name = "Sleeping Cat",
                thumbnailUri = "https://placeholder.com/cat3.jpg",
                resourceUri = "asset:///cats/sleeping_cat.gif",
                type = CatObjectType.GIF
            ),
            CatObject(
                id = "cat_4",
                name = "Running Cat",
                thumbnailUri = "https://placeholder.com/cat4.jpg",
                resourceUri = "asset:///cats/running_cat.mp4",
                type = CatObjectType.VIDEO
            )
        )

        _uiState.update { state ->
            state.copy(availableObjects = mockObjects.toPersistentList())
        }
    }

    /**
     * Update playback position - should be called from ExoPlayer listener
     */
    fun updatePlaybackPosition(positionMs: Long) {
        _uiState.update { state ->
            state.copy(currentPositionMs = positionMs)
        }
    }
}
