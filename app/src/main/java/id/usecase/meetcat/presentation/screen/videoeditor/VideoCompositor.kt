package id.usecase.meetcat.presentation.screen.videoeditor

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

/**
 * Video compositor using Media3 Transformer
 * Handles video composition with backgrounds, objects, and audio
 */
@UnstableApi
class VideoCompositor(private val context: Context) {

    /**
     * Export composed video with all layers
     * Returns Flow of export progress
     */
    fun exportVideo(
        backgroundLayers: List<BackgroundLayer>,
        objectLayers: List<ObjectLayer>,
        audioTracks: List<AudioTrack>,
        outputPath: String,
        totalDurationMs: Long
    ): Flow<ExportProgress> = callbackFlow {
        val transformer = createTransformer(
            onProgress = { progress ->
                trySend(ExportProgress.Progress(progress))
            },
            onComplete = { result ->
                trySend(ExportProgress.Complete(result.durationMs, result.fileSizeBytes))
                close()
            },
            onError = { error ->
                trySend(ExportProgress.Error(error.message ?: "Export failed"))
                close(error)
            }
        )

        try {
            // Create composition with all layers
            val composition = createComposition(
                backgroundLayers = backgroundLayers,
                objectLayers = objectLayers,
                audioTracks = audioTracks,
                totalDurationMs = totalDurationMs
            )

            // Start export
            transformer.start(composition, outputPath)

            // Monitor progress
            val progressHolder = ProgressHolder()
            while (transformer.getProgress(progressHolder) != Transformer.PROGRESS_STATE_NOT_STARTED) {
                if (progressHolder.progress >= 0) {
                    trySend(ExportProgress.Progress(progressHolder.progress / 100f))
                }
                kotlinx.coroutines.delay(100)
            }

        } catch (e: Exception) {
            trySend(ExportProgress.Error(e.message ?: "Export failed"))
            close(e)
        }

        awaitClose {
            transformer.cancel()
        }
    }

    /**
     * Create Transformer instance with callbacks
     */
    private fun createTransformer(
        onProgress: (Float) -> Unit,
        onComplete: (ExportResult) -> Unit,
        onError: (ExportException) -> Unit
    ): Transformer {
        return Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {
                    onComplete(result)
                }

                override fun onError(
                    composition: Composition,
                    result: ExportResult,
                    exception: ExportException
                ) {
                    onError(exception)
                }
            })
            .build()
    }

    /**
     * Create composition from all layers
     */
    private fun createComposition(
        backgroundLayers: List<BackgroundLayer>,
        objectLayers: List<ObjectLayer>,
        audioTracks: List<AudioTrack>,
        totalDurationMs: Long
    ): Composition {
        val editedMediaItems = mutableListOf<EditedMediaItem>()

        // Add background layers
        backgroundLayers.forEach { background ->
            val mediaItem = MediaItem.Builder()
                .setUri(background.uri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(background.startMs)
                        .setEndPositionMs(background.endMs)
                        .build()
                )
                .build()

            val effects = Effects(
                /* audioProcessors */ listOf(),
                /* videoEffects */ listOf(
                    Presentation.createForHeight(1080)
                )
            )

            editedMediaItems.add(
                EditedMediaItem.Builder(mediaItem)
                    .setEffects(effects)
                    .build()
            )
        }

        // TODO: Add object overlays
        // Object overlays require OverlayEffect which is more complex
        // For now, backgrounds are exported

        // Create sequence
        val sequence = EditedMediaItemSequence(editedMediaItems)

        return Composition.Builder(listOf(sequence))
            .setTransmuxVideo(false)
            .setTransmuxAudio(false)
            .build()
    }

    /**
     * Get output file path
     */
    fun getOutputFilePath(): String {
        val outputDir = File(context.cacheDir, "videos")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val timestamp = System.currentTimeMillis()
        return File(outputDir, "meetcat_video_$timestamp.mp4").absolutePath
    }

    /**
     * Get output Uri
     */
    fun getOutputUri(): Uri {
        return getOutputFilePath().toUri()
    }
}

/**
 * Export progress states
 */
sealed class ExportProgress {
    data class Progress(val progress: Float) : ExportProgress()
    data class Complete(val durationMs: Long, val fileSizeBytes: Long) : ExportProgress()
    data class Error(val message: String) : ExportProgress()
}
