package id.usecase.meetcat.presentation.screen.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.usecase.meetcat.domain.usecase.auth.GetCurrentUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountSettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AccountSettingsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadAccountInfo()
    }

    fun onEvent(event: AccountSettingsUiEvent) {
        when (event) {
            is AccountSettingsUiEvent.ChangePassword -> {
                viewModelScope.launch {
                    _uiEffect.send(AccountSettingsUiEffect.ShowChangePasswordDialog)
                }
            }

            is AccountSettingsUiEvent.DeleteAccount -> {
                viewModelScope.launch {
                    _uiEffect.send(AccountSettingsUiEffect.ShowDeleteAccountDialog)
                }
            }

            is AccountSettingsUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(AccountSettingsUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                _uiState.update {
                    it.copy(
                        email = currentUser.email,
                        username = currentUser.username,
                        displayName = currentUser.displayName,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
