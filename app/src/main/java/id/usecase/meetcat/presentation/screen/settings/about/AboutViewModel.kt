package id.usecase.meetcat.presentation.screen.settings.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AboutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<AboutUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onEvent(event: AboutUiEvent) {
        when (event) {
            is AboutUiEvent.NavigateToPrivacyPolicy -> {
                viewModelScope.launch {
                    _uiEffect.send(AboutUiEffect.NavigateToPrivacyPolicy)
                }
            }

            is AboutUiEvent.NavigateToTermsOfService -> {
                viewModelScope.launch {
                    _uiEffect.send(AboutUiEffect.NavigateToTermsOfService)
                }
            }

            is AboutUiEvent.NavigateToLicenses -> {
                viewModelScope.launch {
                    _uiEffect.send(AboutUiEffect.NavigateToLicenses)
                }
            }

            is AboutUiEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEffect.send(AboutUiEffect.NavigateBack)
                }
            }
        }
    }
}
