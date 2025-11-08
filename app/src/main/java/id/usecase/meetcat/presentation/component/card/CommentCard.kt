package id.usecase.meetcat.presentation.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.Comment
import id.usecase.meetcat.presentation.component.user.AvatarSize
import id.usecase.meetcat.presentation.component.user.UserInfo
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommentCard(
    comment: Comment,
    onCommentClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // User Info Section
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    UserInfo(
                        user = comment.user,
                        avatarSize = AvatarSize.Small,
                        timestamp = formatTimestamp(comment.createdAt),
                        onUserClick = onProfileClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable(onClick = onCommentClick)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Love Action
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onLoveClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (comment.isLoved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (comment.isLoved) "Unlove" else "Love",
                            tint = if (comment.isLoved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (comment.lovesCount > 0) {
                        Text(
                            text = comment.lovesCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
private fun CommentCardPreview() {
    MeetCatTheme {
        CommentCard(
            comment = PreviewData.mockComment,
            onCommentClick = {},
            onProfileClick = {},
            onLoveClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentCardLovedPreview() {
    MeetCatTheme {
        CommentCard(
            comment = PreviewData.mockCommentLoved,
            onCommentClick = {},
            onProfileClick = {},
            onLoveClick = {}
        )
    }
}
