package id.usecase.meetcat.presentation.screen.videoeditor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
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
@UnstableApi
class VideoEditorViewModel(
    private val assetManager: VideoEditorAssetManager,
    private val context: Context
) : ViewModel() {

    private val videoCompositor = VideoCompositor(context)

    private val _uiState = MutableStateFlow(VideoEditorUiState())
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<VideoEditorUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadAvailableAssets()
    }

    fun onEvent(event: VideoEditorUiEvent) {
        when (event) {
            // Background Events
            is VideoEditorUiEvent.OnBackgroundSelected -> handleBackgroundSelected(event)
            is VideoEditorUiEvent.OnBackgroundRemoved -> handleBackgroundRemoved(event)

            // Object Events
            is VideoEditorUiEvent.OnObjectSelected -> handleObjectSelected(event)
            is VideoEditorUiEvent.OnObjectClicked -> handleObjectClicked(event)
            is VideoEditorUiEvent.OnObjectMoved -> handleObjectMoved(event)
            is VideoEditorUiEvent.OnObjectRemoved -> handleObjectRemoved(event)
            is VideoEditorUiEvent.OnObjectSoundUpdated -> handleObjectSoundUpdated(event)

            // Audio Events
            is VideoEditorUiEvent.OnAudioSelected -> handleAudioSelected(event)
            is VideoEditorUiEvent.OnAudioRemoved -> handleAudioRemoved(event)
            is VideoEditorUiEvent.OnAudioMoved -> handleAudioMoved(event)

            // Timeline Item Selection
            is VideoEditorUiEvent.OnBackgroundItemClicked -> handleBackgroundItemClicked(event)
            is VideoEditorUiEvent.OnObjectItemClicked -> handleObjectItemClicked(event)
            is VideoEditorUiEvent.OnAudioItemClicked -> handleAudioItemClicked(event)

            // Timeline Item Resize
            is VideoEditorUiEvent.OnBackgroundResized -> handleBackgroundResized(event)
            is VideoEditorUiEvent.OnObjectResized -> handleObjectResized(event)
            is VideoEditorUiEvent.OnAudioResized -> handleAudioResized(event)

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
        // Find the cat object to get default sound
        val catObject = _uiState.value.availableObjects.find { it.id == event.objectId }

        // Create default sound config if object has default sound
        val defaultSoundConfig = if (catObject?.defaultSoundUri != null && catObject.defaultSoundName != null) {
            ObjectSoundConfig(
                soundUri = catObject.defaultSoundUri,
                soundName = catObject.defaultSoundName,
                offsetMs = 0L, // Play immediately when object appears
                durationMs = null, // Play until object disappears
                volume = 1f
            )
        } else null

        val newObject = ObjectLayer(
            id = UUID.randomUUID().toString(),
            objectId = event.objectId,
            startMs = event.startMs,
            endMs = event.endMs,
            soundConfig = defaultSoundConfig // Attach default sound
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

    private fun handleObjectClicked(event: VideoEditorUiEvent.OnObjectClicked) {
        _uiState.update { state ->
            state.copy(selectedObjectForSoundEdit = event.objectId)
        }
    }

    private fun handleObjectSoundUpdated(event: VideoEditorUiEvent.OnObjectSoundUpdated) {
        _uiState.update { state ->
            state.copy(
                objectLayers = state.objectLayers
                    .map { layer ->
                        if (layer.id == event.objectId) {
                            layer.copy(soundConfig = event.soundConfig)
                        } else {
                            layer
                        }
                    }
                    .toPersistentList(),
                selectedObjectForSoundEdit = null // Close dialog
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
        val state = _uiState.value

        // Validate that we have content to export
        if (state.backgroundLayers.isEmpty()) {
            viewModelScope.launch {
                _uiEffect.send(VideoEditorUiEffect.ShowError("Please add at least one background"))
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true, exportProgress = 0f) }

                val outputPath = videoCompositor.getOutputFilePath()

                // Start real export with Media3 Transformer
                videoCompositor.exportVideo(
                    backgroundLayers = state.backgroundLayers.toList(),
                    objectLayers = state.objectLayers.toList(),
                    audioTracks = state.audioTracks.toList(),
                    outputPath = outputPath,
                    totalDurationMs = state.totalDurationMs
                ).collect { progress ->
                    when (progress) {
                        is ExportProgress.Progress -> {
                            _uiState.update { it.copy(exportProgress = progress.progress) }
                        }
                        is ExportProgress.Complete -> {
                            _uiState.update { it.copy(isExporting = false, exportProgress = 1f) }
                            _uiEffect.send(VideoEditorUiEffect.ExportSuccess(
                                videoCompositor.getOutputUri()
                            ))
                        }
                        is ExportProgress.Error -> {
                            _uiState.update { it.copy(isExporting = false, exportProgress = 0f) }
                            _uiEffect.send(VideoEditorUiEffect.ShowError(progress.message))
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportProgress = 0f) }
                _uiEffect.send(VideoEditorUiEffect.ShowError(e.message ?: "Export failed"))
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

    // Timeline Item Selection Handlers
    private fun handleBackgroundItemClicked(event: VideoEditorUiEvent.OnBackgroundItemClicked) {
        _uiState.update { state ->
            state.copy(
                selectedBackgroundId = event.id,
                selectedObjectId = null,
                selectedAudioId = null
            )
        }
    }

    private fun handleObjectItemClicked(event: VideoEditorUiEvent.OnObjectItemClicked) {
        _uiState.update { state ->
            state.copy(
                selectedBackgroundId = null,
                selectedObjectId = event.id,
                selectedAudioId = null
            )
        }
    }

    private fun handleAudioItemClicked(event: VideoEditorUiEvent.OnAudioItemClicked) {
        _uiState.update { state ->
            state.copy(
                selectedBackgroundId = null,
                selectedObjectId = null,
                selectedAudioId = event.id
            )
        }
    }

    // Timeline Item Resize Handlers
    private fun handleBackgroundResized(event: VideoEditorUiEvent.OnBackgroundResized) {
        _uiState.update { state ->
            state.copy(
                backgroundLayers = state.backgroundLayers
                    .map { layer ->
                        if (layer.id == event.id) {
                            layer.copy(
                                startMs = event.newStartMs,
                                endMs = event.newEndMs
                            )
                        } else layer
                    }
                    .toPersistentList()
            )
        }
    }

    private fun handleObjectResized(event: VideoEditorUiEvent.OnObjectResized) {
        _uiState.update { state ->
            state.copy(
                objectLayers = state.objectLayers
                    .map { layer ->
                        if (layer.id == event.id) {
                            layer.copy(
                                startMs = event.newStartMs,
                                endMs = event.newEndMs
                            )
                        } else layer
                    }
                    .toPersistentList()
            )
        }
    }

    private fun handleAudioResized(event: VideoEditorUiEvent.OnAudioResized) {
        _uiState.update { state ->
            state.copy(
                audioTracks = state.audioTracks
                    .map { track ->
                        if (track.id == event.id) {
                            track.copy(
                                startMs = event.newStartMs,
                                endMs = event.newEndMs
                            )
                        } else track
                    }
                    .toPersistentList()
            )
        }
    }

    // Data Loading
    private fun loadAvailableAssets() {
        viewModelScope.launch {
            // Load cat objects from assets
            val catObjects = assetManager.getCatObjects()

            // Load backgrounds from assets
            val backgrounds = assetManager.getBackgrounds()

            // Load sounds from assets
            val sounds = assetManager.getSounds()

            _uiState.update { state ->
                state.copy(
                    availableObjects = catObjects.toPersistentList(),
                    availableBackgrounds = backgrounds.toPersistentList(),
                    availableSounds = sounds.toPersistentList()
                )
            }
        }
    }

    /**
     * Refresh assets - useful after downloading new assets
     */
    fun refreshAssets() {
        loadAvailableAssets()
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
