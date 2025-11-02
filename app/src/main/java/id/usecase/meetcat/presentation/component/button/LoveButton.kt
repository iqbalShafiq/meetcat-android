package id.usecase.meetcat.presentation.component.button

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.usecase.meetcat.ui.theme.MeetCatTheme

@Composable
fun LoveButton(
    isLoved: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isLoved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isLoved) "Unlike" else "Love",
            tint = if (isLoved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoveButtonPreview() {
    MeetCatTheme {
        Row {
            LoveButton(isLoved = false, onClick = {})
            LoveButton(isLoved = true, onClick = {})
        }
    }
}
