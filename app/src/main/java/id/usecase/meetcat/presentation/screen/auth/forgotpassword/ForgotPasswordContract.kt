package id.usecase.meetcat.presentation.screen.auth.forgotpassword

sealed interface ForgotPasswordUiEvent {
    data class EmailChanged(val email: String) : ForgotPasswordUiEvent
    data object ResetPasswordClick : ForgotPasswordUiEvent
    data object NavigateBack : ForgotPasswordUiEvent
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val isResetSent: Boolean = false
)

sealed interface ForgotPasswordUiEffect {
    data object NavigateBack : ForgotPasswordUiEffect
    data class ShowSuccess(val message: String) : ForgotPasswordUiEffect
    data class ShowError(val message: String) : ForgotPasswordUiEffect
}
