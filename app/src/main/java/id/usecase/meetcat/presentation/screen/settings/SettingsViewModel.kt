package id.usecase.meetcat.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.usecase.auth.LogoutUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<SettingsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.NavigateToAccountSettings -> {
                viewModelScope.launch {
                    _uiEffect.send(SettingsUiEffect.NavigateToAccountSettings)
                }
            }

            is SettingsUiEvent.NavigateToPrivacySettings -> {
                viewModelScope.launch {
                    _uiEffect.send(SettingsUiEffect.NavigateToPrivacySettings)
                }
            }

            is SettingsUiEvent.NavigateToAbout -> {
                viewModelScope.launch {
                    _uiEffect.send(SettingsUiEffect.NavigateToAbout)
                }
            }

            is SettingsUiEvent.Logout -> {
                logout()
            }

            is SettingsUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(SettingsUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            logoutUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isLoggingOut = false) }
                    _uiEffect.send(SettingsUiEffect.NavigateToLogin)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoggingOut = false) }
                    _uiEffect.send(SettingsUiEffect.ShowError(error.message ?: "Logout failed"))
                }
        }
    }
}
