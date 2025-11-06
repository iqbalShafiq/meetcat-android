package id.usecase.meetcat.presentation.screen.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<EditProfileUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadCurrentProfile()
    }

    fun onEvent(event: EditProfileUiEvent) {
        when (event) {
            is EditProfileUiEvent.DisplayNameChanged -> {
                _uiState.update { it.copy(displayName = event.displayName, displayNameError = null) }
            }

            is EditProfileUiEvent.UsernameChanged -> {
                _uiState.update { it.copy(username = event.username, usernameError = null) }
            }

            is EditProfileUiEvent.BioChanged -> {
                _uiState.update { it.copy(bio = event.bio, bioError = null) }
            }

            is EditProfileUiEvent.ChangeProfilePhoto -> {
                viewModelScope.launch {
                    _uiEffect.send(EditProfileUiEffect.ShowPhotoPickerDialog)
                }
            }

            is EditProfileUiEvent.SaveChanges -> {
                saveProfile()
            }

            is EditProfileUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(EditProfileUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadCurrentProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                // Mock bio data - in production, this would come from user profile API
                val mockBio = "Cat lover and photographer 🐱📸"

                _uiState.update {
                    it.copy(
                        displayName = currentUser.displayName,
                        username = currentUser.username,
                        email = currentUser.email,
                        profileImageUrl = currentUser.profileImageUrl,
                        bio = mockBio,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
                _uiEffect.send(EditProfileUiEffect.ShowError("Failed to load profile"))
            }
        }
    }

    private fun saveProfile() {
        val currentState = _uiState.value

        // Validate inputs
        if (!validateInputs(currentState.displayName, currentState.username, currentState.bio)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Simulate save operation
            delay(1000)

            _uiState.update { it.copy(isSaving = false) }
            _uiEffect.send(EditProfileUiEffect.ShowSuccess("Profile updated successfully"))
            delay(500)
            _uiEffect.send(EditProfileUiEffect.NavigateBack)
        }
    }

    private fun validateInputs(displayName: String, username: String, bio: String): Boolean {
        var isValid = true

        if (displayName.isBlank()) {
            _uiState.update { it.copy(displayNameError = "Display name cannot be empty") }
            isValid = false
        } else if (displayName.length < 2) {
            _uiState.update { it.copy(displayNameError = "Display name must be at least 2 characters") }
            isValid = false
        }

        if (username.isBlank()) {
            _uiState.update { it.copy(usernameError = "Username cannot be empty") }
            isValid = false
        } else if (username.length < 3) {
            _uiState.update { it.copy(usernameError = "Username must be at least 3 characters") }
            isValid = false
        } else if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _uiState.update { it.copy(usernameError = "Username can only contain letters, numbers, and underscores") }
            isValid = false
        }

        if (bio.length > 160) {
            _uiState.update { it.copy(bioError = "Bio must be 160 characters or less") }
            isValid = false
        }

        return isValid
    }
}
