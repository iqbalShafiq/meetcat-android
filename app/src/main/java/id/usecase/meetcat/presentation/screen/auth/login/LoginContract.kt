package id.usecase.meetcat.presentation.screen.auth.login

sealed interface LoginUiEvent {
    data class EmailChanged(val email: String) : LoginUiEvent
    data class PasswordChanged(val password: String) : LoginUiEvent
    data object LoginClick : LoginUiEvent
    data object NavigateToRegister : LoginUiEvent
    data object NavigateToForgotPassword : LoginUiEvent
    data object TogglePasswordVisibility : LoginUiEvent
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false
)

sealed interface LoginUiEffect {
    data object NavigateToMain : LoginUiEffect
    data object NavigateToRegister : LoginUiEffect
    data object NavigateToForgotPassword : LoginUiEffect
    data class ShowError(val message: String) : LoginUiEffect
}
