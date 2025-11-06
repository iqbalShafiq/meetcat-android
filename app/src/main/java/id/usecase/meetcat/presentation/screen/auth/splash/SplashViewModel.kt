package id.usecase.meetcat.presentation.screen.auth.splash

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

class SplashViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SplashUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAuth = true) }

            // Minimum splash screen duration for better UX
            delay(1000)

            val currentUser = getCurrentUserUseCase()

            if (currentUser != null) {
                _uiEffect.send(SplashUiEffect.NavigateToMain)
            } else {
                _uiEffect.send(SplashUiEffect.NavigateToLogin)
            }

            _uiState.update { it.copy(isCheckingAuth = false) }
        }
    }
}
