package id.usecase.meetcat.presentation.screen.main

import android.net.Uri
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MainUiState(
    val currentRoute: String = "explore",
    val backStack: ImmutableList<String> = persistentListOf("explore"),
    val selectedImageUri: Uri? = null,
    val isNavigatingBack: Boolean = false
)

sealed class MainUiEvent {
    data class NavigateTo(val route: String) : MainUiEvent()
    data object NavigateBack : MainUiEvent()
    data object ShowBottomNav : MainUiEvent()
    data object HideBottomNav : MainUiEvent()
    data class ImageSelected(val uri: Uri) : MainUiEvent()
    data object ClearSelectedImage : MainUiEvent()
    data object ResetNavigationDirection : MainUiEvent()
}

sealed class MainUiEffect {
    data class NavigateToRoute(val route: String) : MainUiEffect()
    data object CloseApp : MainUiEffect()
}
