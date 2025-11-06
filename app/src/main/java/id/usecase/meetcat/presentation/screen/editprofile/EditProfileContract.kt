package id.usecase.meetcat.presentation.screen.editprofile

sealed interface EditProfileUiEvent {
    data class DisplayNameChanged(val displayName: String) : EditProfileUiEvent
    data class UsernameChanged(val username: String) : EditProfileUiEvent
    data class BioChanged(val bio: String) : EditProfileUiEvent
    data object ChangeProfilePhoto : EditProfileUiEvent
    data object SaveChanges : EditProfileUiEvent
    data object NavigateBack : EditProfileUiEvent
}

data class EditProfileUiState(
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val profileImageUrl: String? = null,
    val email: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val displayNameError: String? = null,
    val usernameError: String? = null,
    val bioError: String? = null
)

sealed interface EditProfileUiEffect {
    data object NavigateBack : EditProfileUiEffect
    data object ShowPhotoPickerDialog : EditProfileUiEffect
    data class ShowSuccess(val message: String) : EditProfileUiEffect
    data class ShowError(val message: String) : EditProfileUiEffect
}
