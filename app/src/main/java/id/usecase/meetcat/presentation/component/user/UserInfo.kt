package id.usecase.meetcat.presentation.component.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme

@Composable
fun UserInfo(
    user: User,
    modifier: Modifier = Modifier,
    avatarSize: AvatarSize = AvatarSize.Medium,
    showBio: Boolean = false,
    timestamp: String? = null,
    onUserClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            imageUrl = user.profileImageUrl,
            contentDescription = "Profile picture of ${user.displayName}",
            size = avatarSize,
            onClick = onUserClick
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (timestamp != null) {
                    Text(
                        text = " • $timestamp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showBio && user.bio != null) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserInfoPreview() {
    MeetCatTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            UserInfo(user = PreviewData.mockUser)
            Spacer(modifier = Modifier.width(16.dp))
            UserInfo(user = PreviewData.mockUser, showBio = true)
        }
    }
}
