package id.usecase.meetcat.presentation.screen.settings.privacy

sealed interface PrivacySettingsUiEvent {
    data class PrivateAccountToggled(val isPrivate: Boolean) : PrivacySettingsUiEvent
    data class ShowActivityStatusToggled(val showStatus: Boolean) : PrivacySettingsUiEvent
    data class AllowTaggingToggled(val allowTagging: Boolean) : PrivacySettingsUiEvent
    data object NavigateBack : PrivacySettingsUiEvent
}

data class PrivacySettingsUiState(
    val isPrivateAccount: Boolean = false,
    val showActivityStatus: Boolean = true,
    val allowTagging: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)

sealed interface PrivacySettingsUiEffect {
    data object NavigateBack : PrivacySettingsUiEffect
    data class ShowError(val message: String) : PrivacySettingsUiEffect
    data class ShowSuccess(val message: String) : PrivacySettingsUiEffect
}
