package id.usecase.meetcat.presentation.screen.auth.register

sealed interface RegisterUiEvent {
    data class EmailChanged(val email: String) : RegisterUiEvent
    data class UsernameChanged(val username: String) : RegisterUiEvent
    data class DisplayNameChanged(val displayName: String) : RegisterUiEvent
    data class PasswordChanged(val password: String) : RegisterUiEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterUiEvent
    data object RegisterClick : RegisterUiEvent
    data object NavigateToLogin : RegisterUiEvent
    data object TogglePasswordVisibility : RegisterUiEvent
    data object ToggleConfirmPasswordVisibility : RegisterUiEvent
}

data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val usernameError: String? = null,
    val displayNameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false
)

sealed interface RegisterUiEffect {
    data object NavigateToMain : RegisterUiEffect
    data object NavigateToLogin : RegisterUiEffect
    data class ShowError(val message: String) : RegisterUiEffect
}
