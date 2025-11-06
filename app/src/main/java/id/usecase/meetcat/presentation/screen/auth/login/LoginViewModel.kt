package id.usecase.meetcat.presentation.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<LoginUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null) }
            }

            is LoginUiEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password, passwordError = null) }
            }

            is LoginUiEvent.LoginClick -> {
                login()
            }

            is LoginUiEvent.NavigateToRegister -> {
                viewModelScope.launch {
                    _uiEffect.send(LoginUiEffect.NavigateToRegister)
                }
            }

            is LoginUiEvent.NavigateToForgotPassword -> {
                viewModelScope.launch {
                    _uiEffect.send(LoginUiEffect.NavigateToForgotPassword)
                }
            }

            is LoginUiEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
        }
    }

    private fun login() {
        val currentState = _uiState.value

        // Validate inputs
        if (!validateInputs(currentState.email, currentState.password)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            loginUseCase(currentState.email, currentState.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(LoginUiEffect.NavigateToMain)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(LoginUiEffect.ShowError(error.message ?: "Login failed"))
                }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        } else if (password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }

        return isValid
    }
}
