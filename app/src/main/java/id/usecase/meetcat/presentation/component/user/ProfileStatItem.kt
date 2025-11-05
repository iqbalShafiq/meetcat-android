package id.usecase.meetcat.presentation.component.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import id.usecase.meetcat.ui.theme.MeetCatTheme

/**
 * A reusable stat item component that displays a count and label
 * Commonly used in profile screens, analytics, leaderboards, etc.
 *
 * @param count The numeric value to display
 * @param label The label describing the stat
 * @param modifier Modifier for styling
 * @param onClick Optional click handler for the stat item
 */
@Composable
fun ProfileStatItem(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatStatCount(count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format large numbers with K/M suffix for readability
 */
private fun formatStatCount(count: Int): String {
    return when {
        count >= 1000000 -> String.format("%.1fM", count / 1000000.0)
        count >= 1000 -> String.format("%.1fK", count / 1000.0)
        else -> count.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStatItemPreview() {
    MeetCatTheme {
        ProfileStatItem(
            count = 1234,
            label = "Followers"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStatItemLargePreview() {
    MeetCatTheme {
        ProfileStatItem(
            count = 1234567,
            label = "Views"
        )
    }
}
