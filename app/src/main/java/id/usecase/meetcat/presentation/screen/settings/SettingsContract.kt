package id.usecase.meetcat.presentation.screen.settings

sealed interface SettingsUiEvent {
    data object NavigateToAccountSettings : SettingsUiEvent
    data object NavigateToPrivacySettings : SettingsUiEvent
    data object NavigateToAbout : SettingsUiEvent
    data object Logout : SettingsUiEvent
    data object NavigateBack : SettingsUiEvent
}

data class SettingsUiState(
    val isLoggingOut: Boolean = false
)

sealed interface SettingsUiEffect {
    data object NavigateToAccountSettings : SettingsUiEffect
    data object NavigateToPrivacySettings : SettingsUiEffect
    data object NavigateToAbout : SettingsUiEffect
    data object NavigateToLogin : SettingsUiEffect
    data object NavigateBack : SettingsUiEffect
    data class ShowError(val message: String) : SettingsUiEffect
}
