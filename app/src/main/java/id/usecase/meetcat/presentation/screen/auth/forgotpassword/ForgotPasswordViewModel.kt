package id.usecase.meetcat.presentation.screen.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.usecase.auth.ResetPasswordUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<ForgotPasswordUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: ForgotPasswordUiEvent) {
        when (event) {
            is ForgotPasswordUiEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email, emailError = null) }
            }

            is ForgotPasswordUiEvent.ResetPasswordClick -> {
                resetPassword()
            }

            is ForgotPasswordUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(ForgotPasswordUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun resetPassword() {
        val currentState = _uiState.value

        // Validate email
        if (!validateEmail(currentState.email)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            resetPasswordUseCase(currentState.email)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isResetSent = true) }
                    _uiEffect.send(
                        ForgotPasswordUiEffect.ShowSuccess(
                            "Password reset instructions sent to ${currentState.email}"
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _uiEffect.send(
                        ForgotPasswordUiEffect.ShowError(
                            error.message ?: "Failed to send reset instructions"
                        )
                    )
                }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Invalid email format") }
            return false
        }

        return true
    }
}
