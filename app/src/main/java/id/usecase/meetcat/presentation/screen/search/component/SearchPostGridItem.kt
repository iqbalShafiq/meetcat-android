package id.usecase.meetcat.presentation.screen.search.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import id.usecase.meetcat.R
import id.usecase.meetcat.domain.model.MediaItem
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.ui.theme.MeetCatTheme

@Composable
fun SearchPostGridItem(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstMedia = post.mediaItems.firstOrNull()
    val isVideo = firstMedia is MediaItem.Video

    // Calculate aspect ratio from media dimensions, default to 1:1 if not available
    val aspectRatio = when (firstMedia) {
        is MediaItem.Image -> firstMedia.width.toFloat() / firstMedia.height.toFloat()
        is MediaItem.Video -> firstMedia.width.toFloat() / firstMedia.height.toFloat()
        null -> 1f
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(firstMedia?.thumbnailUrl ?: firstMedia?.url)
                .crossfade(true)
                .build(),
            contentDescription = post.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.ic_placeholder_image),
            error = painterResource(R.drawable.ic_placeholder_image)
        )

        if (isVideo) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPostGridItemPreview() {
    MeetCatTheme {
        SearchPostGridItem(
            post = Post(
                id = "1",
                userId = "user1",
                user = User(
                    id = "user1",
                    username = "catuser",
                    displayName = "Cat User",
                    bio = null,
                    profileImageUrl = null,
                    followersCount = 0,
                    followingCount = 0,
                    postsCount = 0,
                    isFollowing = false,
                    createdAt = System.currentTimeMillis()
                ),
                caption = "Cute cat",
                mediaItems = listOf(
                    MediaItem.Image(
                        url = "https://example.com/cat.jpg",
                        thumbnailUrl = null,
                        width = 800,
                        height = 600
                    )
                ),
                location = null,
                lovesCount = 10,
                commentsCount = 5,
                repliesCount = 2,
                isLoved = false,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchPostGridItemVideoPreview() {
    MeetCatTheme {
        SearchPostGridItem(
            post = Post(
                id = "1",
                userId = "user1",
                user = User(
                    id = "user1",
                    username = "catuser",
                    displayName = "Cat User",
                    bio = null,
                    profileImageUrl = null,
                    followersCount = 0,
                    followingCount = 0,
                    postsCount = 0,
                    isFollowing = false,
                    createdAt = System.currentTimeMillis()
                ),
                caption = "Cute cat video",
                mediaItems = listOf(
                    MediaItem.Video(
                        url = "https://example.com/cat.mp4",
                        thumbnailUrl = "https://example.com/cat_thumb.jpg",
                        duration = 15000,
                        width = 800,
                        height = 600
                    )
                ),
                location = null,
                lovesCount = 10,
                commentsCount = 5,
                repliesCount = 2,
                isLoved = false,
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}
