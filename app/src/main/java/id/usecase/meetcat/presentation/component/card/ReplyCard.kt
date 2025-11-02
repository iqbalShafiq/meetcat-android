package id.usecase.meetcat.presentation.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.Reply
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
fun ReplyCard(
    reply: Reply,
    onReplyClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOriginalPostClick: () -> Unit,
    onOriginalProfileClick: () -> Unit,
    onLoveClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onReplyClick),
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
                user = reply.user,
                avatarSize = AvatarSize.Medium,
                onUserClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reply.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (reply.mediaItems?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                MediaCarousel(mediaItems = reply.mediaItems)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOriginalPostClick),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    UserInfo(
                        user = reply.originalPost.user,
                        avatarSize = AvatarSize.Small,
                        onUserClick = onOriginalProfileClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = reply.originalPost.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )

                    if (reply.originalPost.mediaItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        MediaCarousel(mediaItems = reply.originalPost.mediaItems.take(1))
                    }

                    if (reply.originalPost.location != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LocationChip(location = reply.originalPost.location)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatTimestamp(reply.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            PostActionBar(
                lovesCount = reply.lovesCount,
                commentsCount = reply.commentsCount,
                repliesCount = 0,
                isLoved = reply.isLoved,
                onLoveClick = onLoveClick,
                onCommentClick = onCommentClick,
                onReplyClick = {},
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
private fun ReplyCardPreview() {
    MeetCatTheme {
        ReplyCard(
            reply = PreviewData.mockReply,
            onReplyClick = {},
            onProfileClick = {},
            onOriginalPostClick = {},
            onOriginalProfileClick = {},
            onLoveClick = {},
            onCommentClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReplyCardTextOnlyPreview() {
    MeetCatTheme {
        ReplyCard(
            reply = PreviewData.mockReplyTextOnly,
            onReplyClick = {},
            onProfileClick = {},
            onOriginalPostClick = {},
            onOriginalProfileClick = {},
            onLoveClick = {},
            onCommentClick = {}
        )
    }
}
