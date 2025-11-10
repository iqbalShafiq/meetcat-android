package id.usecase.meetcat.presentation.component.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
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
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme

@Composable
fun UserAvatar(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Medium,
    onClick: (() -> Unit)? = null
) {
    val sizeValue = when (size) {
        AvatarSize.Small -> 32.dp
        AvatarSize.Medium -> 48.dp
        AvatarSize.Large -> 80.dp
    }

    val avatarModifier = modifier
        .size(sizeValue)
        .clip(CircleShape)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = avatarModifier,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.ic_placeholder_avatar),
        error = painterResource(R.drawable.ic_placeholder_avatar)
    )
}

enum class AvatarSize {
    Small, Medium, Large
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarPreview() {
    MeetCatTheme {
        Row {
            UserAvatar(
                imageUrl = PreviewData.mockUser.profileImageUrl,
                contentDescription = "Profile picture",
                size = AvatarSize.Small
            )
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(
                imageUrl = PreviewData.mockUser.profileImageUrl,
                contentDescription = "Profile picture",
                size = AvatarSize.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatar(
                imageUrl = PreviewData.mockUser.profileImageUrl,
                contentDescription = "Profile picture",
                size = AvatarSize.Large
            )
        }
    }
}
