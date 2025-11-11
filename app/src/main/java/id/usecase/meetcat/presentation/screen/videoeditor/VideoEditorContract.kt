package id.usecase.meetcat.presentation.screen.videoeditor

import android.net.Uri
import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Contract for VideoEditor screen
 * Defines the state, events, and effects for the video editor
 */

// UI Events (User Actions)
sealed interface VideoEditorUiEvent {
    // Background Actions
    data class OnBackgroundSelected(val uri: Uri, val startMs: Long, val endMs: Long) : VideoEditorUiEvent
    data class OnBackgroundRemoved(val id: String) : VideoEditorUiEvent

    // Object Actions
    data class OnObjectSelected(val objectId: String, val startMs: Long, val endMs: Long) : VideoEditorUiEvent
    data class OnObjectMoved(val id: String, val newStartMs: Long, val newEndMs: Long) : VideoEditorUiEvent
    data class OnObjectRemoved(val id: String) : VideoEditorUiEvent

    // Audio Actions
    data class OnAudioSelected(val uri: Uri, val startMs: Long, val endMs: Long) : VideoEditorUiEvent
    data class OnAudioRemoved(val id: String) : VideoEditorUiEvent
    data class OnAudioMoved(val id: String, val newStartMs: Long, val newEndMs: Long) : VideoEditorUiEvent

    // Playback Actions
    data object OnPlayPauseClicked : VideoEditorUiEvent
    data class OnSeekTo(val positionMs: Long) : VideoEditorUiEvent

    // Export Actions
    data object OnExportClicked : VideoEditorUiEvent
    data object OnExportCompleted : VideoEditorUiEvent

    // UI Actions
    data class OnTimelineScaleChanged(val scale: Float) : VideoEditorUiEvent
    data object OnBackClicked : VideoEditorUiEvent
    data object OnOpenBackgroundPicker : VideoEditorUiEvent
    data object OnOpenObjectPicker : VideoEditorUiEvent
    data object OnOpenAudioPicker : VideoEditorUiEvent
}

// UI State
@Immutable
data class VideoEditorUiState(
    val backgroundLayers: ImmutableList<BackgroundLayer> = persistentListOf(),
    val objectLayers: ImmutableList<ObjectLayer> = persistentListOf(),
    val audioTracks: ImmutableList<AudioTrack> = persistentListOf(),
    val availableObjects: ImmutableList<CatObject> = persistentListOf(),
    val currentPositionMs: Long = 0,
    val totalDurationMs: Long = 30000, // Default 30 seconds
    val isPlaying: Boolean = false,
    val isExporting: Boolean = false,
    @FloatRange(from = 0.0, to = 1.0)
    val exportProgress: Float = 0f,
    val timelineScale: Float = 1f, // 1f = default zoom level
    val error: String? = null
)

// UI Effects (One-time events)
sealed interface VideoEditorUiEffect {
    data object NavigateBack : VideoEditorUiEffect
    data object OpenBackgroundPicker : VideoEditorUiEffect
    data object OpenObjectPicker : VideoEditorUiEffect
    data object OpenAudioPicker : VideoEditorUiEffect
    data class ShowError(val message: String) : VideoEditorUiEffect
    data class ExportSuccess(val outputUri: Uri) : VideoEditorUiEffect
}

// Data Models

/**
 * Represents a background layer in the timeline
 */
@Immutable
data class BackgroundLayer(
    val id: String,
    val uri: Uri,
    val startMs: Long,
    val endMs: Long,
    val thumbnailUri: Uri? = null
)

/**
 * Represents an object (cat GIF/video) layer in the timeline
 */
@Immutable
data class ObjectLayer(
    val id: String,
    val objectId: String, // Reference to CatObject
    val startMs: Long,
    val endMs: Long,
    val x: Float = 0.5f, // Normalized position (0-1)
    val y: Float = 0.5f, // Normalized position (0-1)
    val scale: Float = 1f
)

/**
 * Represents an audio track in the timeline
 */
@Immutable
data class AudioTrack(
    val id: String,
    val uri: Uri,
    val startMs: Long,
    val endMs: Long,
    val name: String,
    val volume: Float = 1f
)

/**
 * Represents a cat object (GIF/video without background)
 * These are pre-loaded assets that users can select
 */
@Immutable
data class CatObject(
    val id: String,
    val name: String,
    val thumbnailUri: String,
    val resourceUri: String, // Local or remote URI to the asset
    val type: CatObjectType
)

enum class CatObjectType {
    GIF,
    VIDEO
}
