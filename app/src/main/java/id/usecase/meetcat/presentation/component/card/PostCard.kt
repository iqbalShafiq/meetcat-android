package id.usecase.meetcat.presentation.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.Post
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme
import id.usecase.meetcat.presentation.component.common.LocationChip
import id.usecase.meetcat.presentation.component.common.PostActionBar
import id.usecase.meetcat.presentation.component.media.ImageViewer
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
    userLocation: id.usecase.meetcat.domain.model.Location? = null,
    currentUserId: String? = null,
    onEditClick: () -> Unit = {},
    showActions: Boolean = true
) {
    var showImageViewer by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UserInfo(
                    user = post.user,
                    avatarSize = AvatarSize.Medium,
                    timestamp = formatTimestamp(post.createdAt),
                    onUserClick = onProfileClick,
                    modifier = Modifier.weight(1f)
                )

                // Show more menu only if this is the current user's post
                if (currentUserId != null && currentUserId == post.userId) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = {
                                    showMenu = false
                                    // TODO: Implement report functionality
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPostClick)
            ) {
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
                        distanceInMeters = distanceInMeters,
                        onImageClick = { clickedImageUrl ->
                            // Find index of clicked image
                            val index = post.mediaItems
                                .filterIsInstance<id.usecase.meetcat.domain.model.MediaItem.Image>()
                                .indexOfFirst { it.url == clickedImageUrl }

                            selectedImageIndex = if (index >= 0) index else 0
                            showImageViewer = true
                        }
                    )
                }
            }

            if (showActions) {
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

    // Image Viewer Dialog
    if (showImageViewer) {
        // Extract only image URLs (exclude videos)
        val imageUrls = post.mediaItems
            .filterIsInstance<id.usecase.meetcat.domain.model.MediaItem.Image>()
            .map { it.url }

        if (imageUrls.isNotEmpty()) {
            ImageViewer(
                imageUrls = imageUrls,
                initialPage = selectedImageIndex,
                onDismiss = {
                    showImageViewer = false
                }
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
