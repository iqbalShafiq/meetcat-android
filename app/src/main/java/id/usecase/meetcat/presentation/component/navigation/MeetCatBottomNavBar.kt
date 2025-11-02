package id.usecase.meetcat.presentation.component.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.usecase.meetcat.ui.theme.MeetCatTheme

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    Explore("Explore", Icons.Filled.Explore, "explore"),
    Search("Search", Icons.Filled.Search, "search"),
    NearMe("Near Me", Icons.Filled.LocationOn, "nearme"),
    Profile("Profile", Icons.Filled.Person, "profile")
}

@Composable
fun MeetCatBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.dp
        ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeetCatBottomNavBarPreview() {
    MeetCatTheme {
        Box {
            MeetCatBottomNavBar(
                currentRoute = "explore",
                onNavigate = {},
                isVisible = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeetCatBottomNavBarHiddenPreview() {
    MeetCatTheme {
        Box {
            MeetCatBottomNavBar(
                currentRoute = "profile",
                onNavigate = {},
                isVisible = false
            )
        }
    }
}
