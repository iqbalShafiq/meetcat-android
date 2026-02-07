package id.usecase.meetcat.presentation.component.videoeditor

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import id.usecase.meetcat.presentation.screen.videoeditor.BackgroundLayer
import id.usecase.meetcat.presentation.screen.videoeditor.ObjectLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Video preview player component using ExoPlayer
 * Shows preview of composed video with background and object overlays
 */
@Composable
fun VideoPreviewPlayer(
    backgroundLayers: List<BackgroundLayer>,
    objectLayers: List<ObjectLayer>,
    availableObjects: Map<String, String>, // objectId -> resourceUri
    currentPositionMs: Long,
    isPlaying: Boolean,
    selectedObjectId: String?,
    onPositionChanged: (Long) -> Unit,
    onSeek: (Long) -> Unit,
    onObjectClick: (String) -> Unit,
    onObjectPositionChange: (String, Float, Float) -> Unit,
    onObjectScaleChange: (String, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Find active background at current position
    val activeBackground = remember(backgroundLayers, currentPositionMs) {
        backgroundLayers.firstOrNull { bg ->
            currentPositionMs >= bg.startMs && currentPositionMs < bg.endMs
        }
    }

    // Find active objects at current position
    val activeObjects = remember(objectLayers, currentPositionMs) {
        objectLayers.filter { obj ->
            currentPositionMs >= obj.startMs && currentPositionMs < obj.endMs
        }
    }

    // Detect if background is image or video
    val isBackgroundImage = remember(activeBackground) {
        activeBackground?.uri?.toString()?.let { uri ->
            uri.endsWith(".jpg", ignoreCase = true) ||
            uri.endsWith(".jpeg", ignoreCase = true) ||
            uri.endsWith(".png", ignoreCase = true) ||
            uri.endsWith(".webp", ignoreCase = true)
        } ?: false
    }

    Log.d("VideoPreviewPlayer", "Active background: ${activeBackground?.uri}, isImage: $isBackgroundImage")

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
        }
    }

    // Update playing state
    LaunchedEffect(isPlaying) {
        Log.d("VideoPreviewPlayer", "isPlaying changed: $isPlaying")
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    // Update background media when active background changes (for videos only)
    DisposableEffect(activeBackground, isBackgroundImage) {
        if (activeBackground != null && !isBackgroundImage) {
            // Video background - use ExoPlayer
            Log.d("VideoPreviewPlayer", "Loading video: ${activeBackground.uri}")
            val mediaItem = MediaItem.Builder()
                .setUri(activeBackground.uri)
                .build()

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            // Seek to correct position within this background clip
            val relativePosition = currentPositionMs - activeBackground.startMs
            exoPlayer.seekTo(relativePosition.coerceAtLeast(0))
        } else {
            exoPlayer.clearMediaItems()
        }
        onDispose { }
    }

    // Sync seek position for videos
    LaunchedEffect(currentPositionMs, activeBackground, isBackgroundImage) {
        if (!isPlaying && activeBackground != null && !isBackgroundImage) {
            val relativePosition = currentPositionMs - activeBackground.startMs
            if (relativePosition >= 0) {
                exoPlayer.seekTo(relativePosition)
            }
        }
    }

    // Position update loop when playing
    LaunchedEffect(isPlaying, activeBackground) {
        if (isPlaying && activeBackground != null) {
            while (isActive) {
                if (!isBackgroundImage) {
                    // Video - get position from ExoPlayer
                    val currentPos = exoPlayer.currentPosition
                    val absolutePosition = activeBackground.startMs + currentPos
                    onPositionChanged(absolutePosition)
                } else {
                    // Image - simulate position update
                    val newPosition = currentPositionMs + 50
                    if (newPosition < activeBackground.endMs) {
                        onPositionChanged(newPosition)
                    } else {
                        // Move to next layer
                        onPositionChanged(activeBackground.endMs)
                    }
                }
                delay(50) // Update every 50ms
            }
        }
    }

    // Release player on dispose
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Background layer
        if (activeBackground != null) {
            if (isBackgroundImage) {
                // Image background - use AsyncImage
                Log.d("VideoPreviewPlayer", "Rendering image: ${activeBackground.uri}")
                AsyncImage(
                    model = activeBackground.uri,
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                // Video background - use ExoPlayer
                Log.d("VideoPreviewPlayer", "Rendering video with ExoPlayer")
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.matchParentSize()
                )
            }
        } else {
            // Empty state - no background
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(80.dp)
            )
        }

        // Object overlays (cat GIFs) - only show active objects at current timeline position
        activeObjects.forEach { obj ->
            val objectUri = availableObjects[obj.objectId]
            if (objectUri != null) {
                CatObjectOverlay(
                    objectUri = objectUri,
                    x = obj.x,
                    y = obj.y,
                    scale = obj.scale,
                    isSelected = obj.id == selectedObjectId,
                    onPositionChange = { newX, newY ->
                        onObjectPositionChange(obj.id, newX, newY)
                    },
                    onScaleChange = { scaleDelta ->
                        val newScale = (obj.scale + scaleDelta).coerceIn(0.5f, 3f)
                        onObjectScaleChange(obj.id, newScale)
                    },
                    onClick = {
                        onObjectClick(obj.id)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Cat object overlay composable with drag and resize support
 */
@Composable
private fun CatObjectOverlay(
    objectUri: String,
    x: Float, // Normalized 0-1
    y: Float, // Normalized 0-1
    scale: Float,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onPositionChange: ((Float, Float) -> Unit)? = null,
    onScaleChange: ((Float) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        // Use BoxWithConstraints to access parent size
        androidx.compose.foundation.layout.BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val density = LocalDensity.current
            val containerWidth = maxWidth
            val containerHeight = maxHeight

            val objectSizeDp = 120.dp * scale
            val offsetX = (containerWidth.value * x).dp - (objectSizeDp / 2)
            val offsetY = (containerHeight.value * y).dp - (objectSizeDp / 2)

            var dragOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

            Box(
                modifier = Modifier
                    .size(objectSizeDp)
                    .offset(x = offsetX, y = offsetY)
            ) {
                // The cat GIF
                AsyncImage(
                    model = objectUri.toUri(),
                    contentDescription = "Cat object",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClick?.invoke() }
                        .then(
                            if (onPositionChange != null) {
                                Modifier.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            dragOffset = androidx.compose.ui.geometry.Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            // Calculate new position in normalized coordinates
                                            val newX = ((offsetX.value + dragOffset.x / density.density) / containerWidth.value)
                                                .coerceIn(0f, 1f)
                                            val newY = ((offsetY.value + dragOffset.y / density.density) / containerHeight.value)
                                                .coerceIn(0f, 1f)

                                            onPositionChange(newX, newY)
                                        }
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                )

                // Selection border
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )

                    // Resize handles (corners)
                    ResizeHandle(
                        alignment = Alignment.TopStart,
                        onScaleChange = onScaleChange
                    )
                    ResizeHandle(
                        alignment = Alignment.TopEnd,
                        onScaleChange = onScaleChange
                    )
                    ResizeHandle(
                        alignment = Alignment.BottomStart,
                        onScaleChange = onScaleChange
                    )
                    ResizeHandle(
                        alignment = Alignment.BottomEnd,
                        onScaleChange = onScaleChange
                    )
                }
            }
        }
    }
}

/**
 * Resize handle for object scaling
 */
@Composable
private fun BoxScope.ResizeHandle(
    alignment: Alignment,
    onScaleChange: ((Float) -> Unit)?
) {
    if (onScaleChange != null) {
        Box(
            modifier = Modifier
                .align(alignment)
                .size(16.dp)
                .offset(
                    x = when (alignment) {
                        Alignment.TopStart, Alignment.BottomStart -> (-8).dp
                        Alignment.TopEnd, Alignment.BottomEnd -> 8.dp
                        else -> 0.dp
                    },
                    y = when (alignment) {
                        Alignment.TopStart, Alignment.TopEnd -> (-8).dp
                        Alignment.BottomStart, Alignment.BottomEnd -> 8.dp
                        else -> 0.dp
                    }
                )
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Simple scale: drag away = bigger, drag toward = smaller
                        val scaleDelta = (dragAmount.x + dragAmount.y) / 500f
                        onScaleChange(scaleDelta)
                    }
                }
        )
    }
}
