package id.usecase.meetcat.presentation.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme
import id.usecase.meetcat.presentation.component.common.LocationChip
import id.usecase.meetcat.presentation.component.common.PostActionBar
import id.usecase.meetcat.presentation.component.media.MediaCarousel
import id.usecase.meetcat.presentation.component.user.AvatarSize
import id.usecase.meetcat.presentation.component.user.UserInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PostCard(
    post: Post,
    onPostClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReplyClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    userLocation: id.usecase.meetcat.domain.model.Location? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            UserInfo(
                user = post.user,
                avatarSize = AvatarSize.Medium,
                timestamp = formatTimestamp(post.createdAt),
                onUserClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (post.mediaItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                val distanceInMeters = if (post.location != null && userLocation != null) {
                    id.usecase.meetcat.presentation.util.calculateDistance(
                        userLocation,
                        post.location
                    )
                } else null

                MediaCarousel(
                    mediaItems = post.mediaItems,
                    location = post.location,
                    distanceInMeters = distanceInMeters
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PostActionBar(
                lovesCount = post.lovesCount,
                commentsCount = post.commentsCount,
                repliesCount = post.repliesCount,
                isLoved = post.isLoved,
                onLoveClick = onLoveClick,
                onCommentClick = onCommentClick,
                onReplyClick = onReplyClick,
                onShareClick = onShareClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    MeetCatTheme {
        PostCard(
            post = PreviewData.mockPost,
            onPostClick = {},
            onProfileClick = {},
            onLoveClick = {},
            onCommentClick = {},
            onReplyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardLovedPreview() {
    MeetCatTheme {
        PostCard(
            post = PreviewData.mockPostLoved,
            onPostClick = {},
            onProfileClick = {},
            onLoveClick = {},
            onCommentClick = {},
            onReplyClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostCardWithVideoPreview() {
    MeetCatTheme {
        PostCard(
            post = PreviewData.mockPostWithVideo,
            onPostClick = {},
            onProfileClick = {},
            onLoveClick = {},
            onCommentClick = {},
            onReplyClick = {}
        )
    }
}
