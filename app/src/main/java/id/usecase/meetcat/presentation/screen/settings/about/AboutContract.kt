package id.usecase.meetcat.presentation.screen.settings.about

sealed interface AboutUiEvent {
    data object NavigateToPrivacyPolicy : AboutUiEvent
    data object NavigateToTermsOfService : AboutUiEvent
    data object NavigateToLicenses : AboutUiEvent
    data object NavigateBack : AboutUiEvent
}

data class AboutUiState(
    val appName: String = "MeetCat",
    val version: String = "1.0.0",
    val buildNumber: String = "1"
)

sealed interface AboutUiEffect {
    data object NavigateToPrivacyPolicy : AboutUiEffect
    data object NavigateToTermsOfService : AboutUiEffect
    data object NavigateToLicenses : AboutUiEffect
    data object NavigateBack : AboutUiEffect
}
