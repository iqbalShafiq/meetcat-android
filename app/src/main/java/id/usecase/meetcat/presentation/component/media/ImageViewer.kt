package id.usecase.meetcat.presentation.component.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import id.usecase.meetcat.ui.theme.MeetCatTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = {
            isVisible = false
            // Delay actual dismiss to allow exit animation
            coroutineScope.launch {
                delay(200)
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                        val newScale = (scale * zoomChange).coerceIn(1f, 5f)

                        // Only allow panning if zoomed in
                        if (newScale > 1f || scale > 1f) {
                            scale = newScale
                            offsetX += panChange.x
                            offsetY += panChange.y
                            isDragging = true
                        }
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Full screen image",
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(
                                    offsetX.roundToInt(),
                                    offsetY.roundToInt()
                                )
                            }
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                            .transformable(state = transformableState)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = { tapOffset ->
                                        if (scale > 1f) {
                                            // Reset zoom
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            // Zoom to 2x at tap position
                                            scale = 2f
                                            // Calculate offset to center on tap
                                            val centerX = size.width / 2f
                                            val centerY = size.height / 2f
                                            offsetX = (centerX - tapOffset.x) * scale
                                            offsetY = (centerY - tapOffset.y) * scale
                                        }
                                    }
                                )
                            }
                            .pointerInput(scale) {
                                // Swipe down to dismiss (only when not zoomed)
                                if (scale == 1f) {
                                    detectDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = {
                                            isDragging = false
                                            if (abs(offsetY) > 200f) {
                                                isVisible = false
                                                coroutineScope.launch {
                                                    delay(200)
                                                    onDismiss()
                                                }
                                            } else {
                                                // Animate back to center
                                                offsetY = 0f
                                            }
                                        },
                                        onDragCancel = {
                                            isDragging = false
                                            offsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offsetY += dragAmount.y
                                        }
                                    )
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Close button
            IconButton(
                onClick = {
                    isVisible = false
                    coroutineScope.launch {
                        delay(200)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close image viewer",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageViewerPreview() {
    MeetCatTheme {
        ImageViewer(
            imageUrl = "https://example.com/cat.jpg",
            onDismiss = {}
        )
    }
}
