package id.usecase.meetcat.presentation.component.util

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Custom NestedScrollConnection untuk bottom navbar auto-hide/show behavior.
 * Ini akan track scroll delta dan callback onScroll untuk parent ViewModel.
 */
class BottomNavBarNestedScrollConnection(
    private val onScroll: (scrollDelta: Float) -> Unit
) : NestedScrollConnection {

    override fun onPreScroll(
        available: androidx.compose.ui.geometry.Offset,
        source: NestedScrollSource
    ): androidx.compose.ui.geometry.Offset {
        // Capture the scroll delta (positive = scroll down, negative = scroll up)
        // available.y is the scroll delta: positive for down, negative for up
        onScroll(available.y)
        return androidx.compose.ui.geometry.Offset.Zero
    }

    override suspend fun onPreFling(velocity: Velocity): Velocity {
        return Velocity.Zero
    }
}

@Composable
fun rememberBottomNavBarScrollBehavior(
    onScroll: (scrollDelta: Float) -> Unit
): NestedScrollConnection {
    return remember {
        BottomNavBarNestedScrollConnection(onScroll)
    }
}
