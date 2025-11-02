package id.usecase.meetcat.presentation.screen.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Simple boolean for navbar visibility
    var isBottomNavVisible by mutableStateOf(true)
        private set

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.NavigateTo -> navigateTo(event.route)
            is MainUiEvent.ShowBottomNav -> showBottomNav()
            is MainUiEvent.HideBottomNav -> hideBottomNav()
        }
    }

    private fun navigateTo(route: String) {
        _uiState.update { it.copy(currentRoute = route) }
    }

    private fun showBottomNav() {
        isBottomNavVisible = true
    }

    private fun hideBottomNav() {
        isBottomNavVisible = false
    }
}
