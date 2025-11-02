package id.usecase.meetcat.presentation.component.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewer(
    imageUrls: List<String>,
    initialPage: Int = 0,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, imageUrls.size - 1),
        pageCount = { imageUrls.size }
    )
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
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        ZoomableImage(
                            imageUrl = imageUrls[page],
                            onSwipeDown = {
                                isVisible = false
                                coroutineScope.launch {
                                    delay(200)
                                    onDismiss()
                                }
                            }
                        )
                    }
                }
            }

            // Page indicator dots (only show if multiple images)
            if (imageUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(imageUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.5f)
                                    }
                                )
                        )
                    }
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

@Composable
private fun ZoomableImage(
    imageUrl: String,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Full screen image",
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                translationX = offsetX.value,
                translationY = offsetY.value
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        coroutineScope.launch {
                            if (scale.value > 1f) {
                                // Reset zoom with animation
                                scale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                offsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                offsetY.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            } else {
                                // Zoom to 2x at tap position with animation
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val targetOffsetX = (centerX - tapOffset.x) * 2f
                                val targetOffsetY = (centerY - tapOffset.y) * 2f

                                scale.animateTo(
                                    2f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                offsetX.animateTo(
                                    targetOffsetX,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                offsetY.animateTo(
                                    targetOffsetY,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        // Handle zoom (pinch gesture)
                        if (zoom != 1f) {
                            val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                            coroutineScope.launch {
                                scale.snapTo(newScale)
                            }
                        }

                        // Handle pan
                        if (scale.value > 1f) {
                            // Allow panning when zoomed in
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + pan.x)
                                offsetY.snapTo(offsetY.value + pan.y)
                            }
                        } else {
                            // Swipe down to dismiss when not zoomed
                            if (event.changes.any { it.positionChanged() }) {
                                coroutineScope.launch {
                                    offsetY.snapTo(offsetY.value + pan.y)
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // On gesture end
                    if (scale.value == 1f && abs(offsetY.value) > 200f) {
                        // Dismiss if swiped down enough
                        onSwipeDown()
                    } else if (scale.value == 1f) {
                        // Animate back to center
                        coroutineScope.launch {
                            offsetY.animateTo(
                                0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    }
                }
            },
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
private fun ImageViewerPreview() {
    MeetCatTheme {
        ImageViewer(
            imageUrls = listOf("https://example.com/cat.jpg"),
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageViewerMultiplePreview() {
    MeetCatTheme {
        ImageViewer(
            imageUrls = listOf(
                "https://example.com/cat1.jpg",
                "https://example.com/cat2.jpg",
                "https://example.com/cat3.jpg"
            ),
            initialPage = 1,
            onDismiss = {}
        )
    }
}
