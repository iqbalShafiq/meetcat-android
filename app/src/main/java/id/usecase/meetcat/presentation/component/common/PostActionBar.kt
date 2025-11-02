package id.usecase.meetcat.presentation.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.presentation.component.button.LoveButton
import id.usecase.meetcat.ui.theme.MeetCatTheme

@Composable
fun PostActionBar(
    lovesCount: Int,
    commentsCount: Int,
    repliesCount: Int,
    isLoved: Boolean,
    onLoveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReplyClick: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LoveButton(
                isLoved = isLoved,
                onClick = onLoveClick
            )
            if (lovesCount > 0) {
                Text(
                    text = lovesCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCommentClick) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (commentsCount > 0) {
                Text(
                    text = commentsCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onReplyClick) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (repliesCount > 0) {
                Text(
                    text = repliesCount.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostActionBarPreview() {
    MeetCatTheme {
        PostActionBar(
            lovesCount = 234,
            commentsCount = 45,
            repliesCount = 12,
            isLoved = false,
            onLoveClick = {},
            onCommentClick = {},
            onReplyClick = {},
            onShareClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PostActionBarLovedPreview() {
    MeetCatTheme {
        PostActionBar(
            lovesCount = 235,
            commentsCount = 45,
            repliesCount = 12,
            isLoved = true,
            onLoveClick = {},
            onCommentClick = {},
            onReplyClick = {},
            onShareClick = {}
        )
    }
}
