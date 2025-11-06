package id.usecase.meetcat.presentation.screen.settings.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrivacySettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<PrivacySettingsUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        loadPrivacySettings()
    }

    fun onEvent(event: PrivacySettingsUiEvent) {
        when (event) {
            is PrivacySettingsUiEvent.PrivateAccountToggled -> {
                _uiState.update { it.copy(isPrivateAccount = event.isPrivate) }
                saveSettings()
            }

            is PrivacySettingsUiEvent.ShowActivityStatusToggled -> {
                _uiState.update { it.copy(showActivityStatus = event.showStatus) }
                saveSettings()
            }

            is PrivacySettingsUiEvent.AllowTaggingToggled -> {
                _uiState.update { it.copy(allowTagging = event.allowTagging) }
                saveSettings()
            }

            is PrivacySettingsUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(PrivacySettingsUiEffect.NavigateBack)
                }
            }
        }
    }

    private fun loadPrivacySettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate loading
            delay(500)

            // TODO: Load from repository
            _uiState.update {
                it.copy(
                    isPrivateAccount = false,
                    showActivityStatus = true,
                    allowTagging = true,
                    isLoading = false
                )
            }
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Simulate save
            delay(500)

            _uiState.update { it.copy(isSaving = false) }
            _uiEffect.send(PrivacySettingsUiEffect.ShowSuccess("Settings saved"))
        }
    }
}
