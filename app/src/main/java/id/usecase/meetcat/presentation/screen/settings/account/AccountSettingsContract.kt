package id.usecase.meetcat.presentation.screen.settings.account

sealed interface AccountSettingsUiEvent {
    data object ChangePassword : AccountSettingsUiEvent
    data object DeleteAccount : AccountSettingsUiEvent
    data object NavigateBack : AccountSettingsUiEvent
}

data class AccountSettingsUiState(
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false
)

sealed interface AccountSettingsUiEffect {
    data object NavigateBack : AccountSettingsUiEffect
    data object ShowChangePasswordDialog : AccountSettingsUiEffect
    data object ShowDeleteAccountDialog : AccountSettingsUiEffect
    data class ShowError(val message: String) : AccountSettingsUiEffect
    data class ShowSuccess(val message: String) : AccountSettingsUiEffect
}
