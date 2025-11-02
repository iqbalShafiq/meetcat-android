package id.usecase.meetcat.presentation.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.domain.model.Location
import id.usecase.meetcat.presentation.preview.PreviewData
import id.usecase.meetcat.ui.theme.MeetCatTheme

@Composable
fun LocationBadge(
    location: Location,
    distanceInMeters: Float?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Color.White
        )

        val text = buildString {
            append(location.name ?: location.address ?: "Unknown")
            if (distanceInMeters != null) {
                append(" • ")
                append(formatDistance(distanceInMeters))
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

private fun formatDistance(meters: Float): String {
    return when {
        meters < 1000 -> "${meters.toInt()}m"
        meters < 10000 -> String.format("%.1f km", meters / 1000)
        else -> "${(meters / 1000).toInt()} km"
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationBadgePreview() {
    MeetCatTheme {
        LocationBadge(
            location = PreviewData.mockLocation,
            distanceInMeters = 250f
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationBadgeKilometersPreview() {
    MeetCatTheme {
        LocationBadge(
            location = PreviewData.mockLocation,
            distanceInMeters = 1500f
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationBadgeNoDistancePreview() {
    MeetCatTheme {
        LocationBadge(
            location = PreviewData.mockLocation,
            distanceInMeters = null
        )
    }
}
