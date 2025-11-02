package id.usecase.meetcat.presentation.component.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun OnScrollChanged(
    lazyListState: LazyListState,
    onScroll: (scrollOffset: Int) -> Unit
) {
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .map { scrollOffset ->
                // Convert scroll offset to approximate vertical scroll
                (lazyListState.firstVisibleItemIndex * 100) + scrollOffset
            }
            .distinctUntilChanged()
            .collect { totalScroll ->
                onScroll(totalScroll)
            }
    }
}
