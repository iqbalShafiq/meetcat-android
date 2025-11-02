package id.usecase.meetcat.presentation.component.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import id.usecase.meetcat.domain.model.MediaItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaCarousel(
    mediaItems: List<MediaItem>,
    modifier: Modifier = Modifier
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
    }
}
