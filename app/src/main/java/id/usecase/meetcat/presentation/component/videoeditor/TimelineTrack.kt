package id.usecase.meetcat.presentation.component.videoeditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

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
    onItemClick: (String) -> Unit = {},
    onItemDrag: (String, Long, Long) -> Unit = { _, _, _ -> }
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                        detectHorizontalDragGestures { change, dragAmount ->
                            // TODO: Handle drag to move items
                            change.consume()
                        }
                    }
                    .pointerInput(items, totalDurationMs) {
                        // Handle click to select item
                        androidx.compose.foundation.gestures.detectTapGestures { offset ->
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

                    // Draw item rectangle
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(startX, 8.dp.toPx()),
                        size = Size(width, size.height - 16.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )

                    // Draw border
                    drawRoundRect(
                        color = color.copy(alpha = 0.5f),
                        topLeft = Offset(startX, 8.dp.toPx()),
                        size = Size(width, size.height - 16.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

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
                    color = MaterialTheme.colorScheme.primary,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
    }
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
