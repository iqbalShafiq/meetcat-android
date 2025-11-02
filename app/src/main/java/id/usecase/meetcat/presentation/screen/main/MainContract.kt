package id.usecase.meetcat.presentation.screen.main

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MainUiState(
    val currentRoute: String = "explore"
)

sealed class MainUiEvent {
    data class NavigateTo(val route: String) : MainUiEvent()
    data object ShowBottomNav : MainUiEvent()
    data object HideBottomNav : MainUiEvent()
}

sealed class MainUiEffect {
    data class NavigateToRoute(val route: String) : MainUiEffect()
}
