package id.usecase.meetcat.presentation.screen.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<RegisterUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null) }
            }

            is RegisterUiEvent.UsernameChanged -> {
                _uiState.update { it.copy(username = event.username, usernameError = null) }
            }

            is RegisterUiEvent.DisplayNameChanged -> {
                _uiState.update { it.copy(displayName = event.displayName, displayNameError = null) }
            }

            is RegisterUiEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, passwordError = null) }
            }

            is RegisterUiEvent.ConfirmPasswordChanged -> {
                _uiState.update { it.copy(confirmPassword = event.confirmPassword, confirmPasswordError = null) }
            }

            is RegisterUiEvent.RegisterClick -> {
                register()
            }

            is RegisterUiEvent.NavigateToLogin -> {
                viewModelScope.launch {
                    _uiEffect.send(RegisterUiEffect.NavigateToLogin)
                }
            }

            is RegisterUiEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }

            is RegisterUiEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
        }
    }

    private fun register() {
        val currentState = _uiState.value

        // Validate inputs
        if (!validateInputs(
                currentState.email,
                currentState.username,
                currentState.displayName,
                currentState.password,
                currentState.confirmPassword
            )
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            registerUseCase(
                email = currentState.email,
                username = currentState.username,
                displayName = currentState.displayName,
                password = currentState.password
            )
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(RegisterUiEffect.NavigateToMain)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(RegisterUiEffect.ShowError(error.message ?: "Registration failed"))
                }
        }
    }

    private fun validateInputs(
        email: String,
        username: String,
        displayName: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
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

        if (displayName.isBlank()) {
            _uiState.update { it.copy(displayNameError = "Display name cannot be empty") }
            isValid = false
        } else if (displayName.length < 2) {
            _uiState.update { it.copy(displayNameError = "Display name must be at least 2 characters") }
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Please confirm your password") }
            isValid = false
        } else if (password != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match") }
            isValid = false
        }

        return isValid
    }
}
