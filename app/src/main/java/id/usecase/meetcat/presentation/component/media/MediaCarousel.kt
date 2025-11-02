package id.usecase.meetcat.presentation.component.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.presentation.component.common.LocationBadge

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCarousel(
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier,
    location: Location? = null,
    distanceInMeters: Float? = null
) {
    if (mediaItems.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { mediaItems.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (val mediaItem = mediaItems[page]) {
                is MediaItem.Image -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaItem.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post image ${page + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                is MediaItem.Video -> {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaItem.thumbnailUrl ?: mediaItem.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Video thumbnail ${page + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // Bottom overlay with page indicator and location badge
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left spacer to balance the layout
            Box(modifier = Modifier.weight(1f))

            // Page indicator dots (only show if multiple items)
            if (mediaItems.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(mediaItems.size) { index ->
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
            } else {
                // Empty box to maintain layout when no indicator
                Box(modifier = Modifier)
            }

            // Right side with location badge
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (location != null) {
                    LocationBadge(
                        location = location,
                        distanceInMeters = distanceInMeters
                    )
                }
            }
        }
    }
}
