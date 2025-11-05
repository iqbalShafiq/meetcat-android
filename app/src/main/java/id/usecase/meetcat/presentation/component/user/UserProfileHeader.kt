package id.usecase.meetcat.presentation.component.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.User
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme

/**
 * Profile stat data for UserProfileHeader
 */
data class ProfileStat(
    val count: Int,
    val label: String,
    val onClick: (() -> Unit)? = null
)

/**
 * A reusable profile header component with centered layout
 * Displays user avatar, name, username, bio, and stats
 * Commonly used in profile screens, user detail sheets, etc.
 *
 * @param user User data to display
 * @param stats List of stats to display (e.g., posts, followers, following)
 * @param modifier Modifier for styling
 * @param onAvatarClick Optional click handler for avatar
 */
@Composable
fun UserProfileHeader(
    user: User,
    stats: List<ProfileStat>,
    modifier: Modifier = Modifier,
    onAvatarClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Photo
        UserAvatar(
            imageUrl = user.profileImageUrl,
            size = UserAvatarSize.Large,
            onClick = onAvatarClick,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Name
        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Username
        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Bio
        if (!user.bio.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Stats
        if (stats.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { stat ->
                    ProfileStatItem(
                        count = stat.count,
                        label = stat.label,
                        onClick = stat.onClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileHeaderPreview() {
    MeetCatTheme {
        UserProfileHeader(
            user = PreviewData.mockUser,
            stats = listOf(
                ProfileStat(count = 156, label = "Posts"),
                ProfileStat(count = 2456, label = "Followers"),
                ProfileStat(count = 342, label = "Following")
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileHeaderNoBioPreview() {
    MeetCatTheme {
        UserProfileHeader(
            user = PreviewData.mockUser.copy(bio = null),
            stats = listOf(
                ProfileStat(count = 42, label = "Posts"),
                ProfileStat(count = 1234567, label = "Loves")
            )
        )
    }
}
