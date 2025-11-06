package id.usecase.meetcat.presentation.screen.auth.splash

sealed interface SplashUiEvent {
    // No user events for splash screen
}

data class SplashUiState(
    val isCheckingAuth: Boolean = true
)

sealed interface SplashUiEffect {
    data object NavigateToMain : SplashUiEffect
    data object NavigateToLogin : SplashUiEffect
}
