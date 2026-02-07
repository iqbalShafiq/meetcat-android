package id.usecase.meetcat.presentation.component.videoeditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

/**
 * Timeline track component for video editor
 * Shows items on a timeline with playhead
 */
@Composable
fun TimelineTrack(
    label: String,
    items: List<TimelineItem>,
    totalDurationMs: Long,
    currentPositionMs: Long,
    color: Color,
    modifier: Modifier = Modifier,
    showSoundIndicator: Boolean = false,
    selectedItemId: String? = null,
    onItemClick: (String) -> Unit = {},
    onItemResize: (String, Long, Long) -> Unit = { _, _, _ -> }
) {
    // Get color from MaterialTheme before Canvas block
    val playheadColor = MaterialTheme.colorScheme.primary

    // Track which edge is being dragged
    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var dragEdge by remember { mutableStateOf<DragEdge?>(null) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .pointerInput(items, totalDurationMs) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Detect if drag started near edge of an item
                                val clickX = offset.x
                                val edgeThreshold = 16.dp.toPx()

                                items.forEach { item ->
                                    val startX = (item.startMs.toFloat() / totalDurationMs) * size.width
                                    val endX = (item.endMs.toFloat() / totalDurationMs) * size.width

                                    // Check if near left edge
                                    if ((clickX - startX).absoluteValue < edgeThreshold) {
                                        draggedItemId = item.id
                                        dragEdge = DragEdge.LEFT
                                    }
                                    // Check if near right edge
                                    else if ((clickX - endX).absoluteValue < edgeThreshold) {
                                        draggedItemId = item.id
                                        dragEdge = DragEdge.RIGHT
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                draggedItemId?.let { itemId ->
                                    val item = items.find { it.id == itemId }
                                    item?.let {
                                        val deltaMs = ((dragAmount.x / size.width) * totalDurationMs).toLong()

                                        when (dragEdge) {
                                            DragEdge.LEFT -> {
                                                // Resize from left (adjust start time)
                                                val newStartMs = (it.startMs + deltaMs).coerceIn(0, it.endMs - 500) // Min 500ms duration
                                                onItemResize(itemId, newStartMs, it.endMs)
                                            }
                                            DragEdge.RIGHT -> {
                                                // Resize from right (adjust end time)
                                                val newEndMs = (it.endMs + deltaMs).coerceIn(it.startMs + 500, totalDurationMs)
                                                onItemResize(itemId, it.startMs, newEndMs)
                                            }
                                            null -> {}
                                        }
                                    }
                                }
                                change.consume()
                            },
                            onDragEnd = {
                                draggedItemId = null
                                dragEdge = null
                            }
                        )
                    }
                    .pointerInput(items, totalDurationMs) {
                        // Handle click to select item
                        detectTapGestures { offset ->
                            // Find which item was clicked
                            val clickX = offset.x
                            items.forEach { item ->
                                val startX = (item.startMs.toFloat() / totalDurationMs) * size.width
                                val width = ((item.endMs - item.startMs).toFloat() / totalDurationMs) * size.width
                                if (clickX >= startX && clickX <= startX + width) {
                                    onItemClick(item.id)
                                }
                            }
                        }
                    }
            ) {
                // Draw timeline items
                items.forEach { item ->
                    val startX = (item.startMs.toFloat() / totalDurationMs) * size.width
                    val width = ((item.endMs - item.startMs).toFloat() / totalDurationMs) * size.width
                    val isSelected = item.id == selectedItemId

                    // Generate unique color for each item based on ID
                    val itemColor = generateColorFromId(item.id, color)

                    // Draw item rectangle
                    drawRoundRect(
                        color = itemColor,
                        topLeft = Offset(startX, 8.dp.toPx()),
                        size = Size(width, size.height - 16.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )

                    // Draw border - thicker and different color when selected
                    drawRoundRect(
                        color = if (isSelected) Color.White else itemColor.copy(alpha = 0.5f),
                        topLeft = Offset(startX, 8.dp.toPx()),
                        size = Size(width, size.height - 16.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = if (isSelected) 3.dp.toPx() else 2.dp.toPx()
                        )
                    )

                    // Draw resize handles on selected item
                    if (isSelected) {
                        val handleWidth = 4.dp.toPx()
                        val handleHeight = size.height - 16.dp.toPx()

                        // Left handle
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(startX, 8.dp.toPx()),
                            size = Size(handleWidth, handleHeight)
                        )

                        // Right handle
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(startX + width - handleWidth, 8.dp.toPx()),
                            size = Size(handleWidth, handleHeight)
                        )
                    }

                    // Draw sound indicator if item has sound
                    if (showSoundIndicator && item.hasSound) {
                        drawCircle(
                            color = Color.Yellow,
                            radius = 6.dp.toPx(),
                            center = Offset(startX + width - 12.dp.toPx(), 8.dp.toPx() + 8.dp.toPx())
                        )
                        // Draw musical note symbol (approximation)
                        drawCircle(
                            color = Color.Black,
                            radius = 3.dp.toPx(),
                            center = Offset(startX + width - 12.dp.toPx(), 8.dp.toPx() + 8.dp.toPx())
                        )
                    }
                }

                // Draw playhead
                val playheadX = (currentPositionMs.toFloat() / totalDurationMs) * size.width
                drawLine(
                    color = playheadColor,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
    }
}

/**
 * Generate a unique color for an item based on its ID
 */
private fun generateColorFromId(id: String, baseColor: Color): Color {
    val hash = id.hashCode()
    val hue = (hash and 0xFF) / 255f // 0-1
    val saturation = 0.5f + ((hash shr 8 and 0xFF) / 255f) * 0.3f // 0.5-0.8
    val lightness = 0.5f + ((hash shr 16 and 0xFF) / 255f) * 0.2f // 0.5-0.7

    return Color.hsl(hue * 360f, saturation, lightness)
}

/**
 * Enum for drag edge detection
 */
private enum class DragEdge {
    LEFT, RIGHT
}

/**
 * Data class representing an item on the timeline
 */
data class TimelineItem(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val label: String = "",
    val hasSound: Boolean = false
)
