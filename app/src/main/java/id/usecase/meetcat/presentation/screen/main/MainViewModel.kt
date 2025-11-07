package id.usecase.meetcat.presentation.screen.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import id.usecase.meetcat.presentation.component.navigation.BottomNavItem
import kotlinx.collections.immutable.toImmutableList
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

    // Bottom nav routes for special handling
    private val bottomNavRoutes = setOf(
        BottomNavItem.Explore.route,
        BottomNavItem.Search.route,
        BottomNavItem.NearMe.route,
        BottomNavItem.Profile.route
    )

    fun onEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.NavigateTo -> navigateTo(event.route)
            is MainUiEvent.NavigateBack -> navigateBack()
            is MainUiEvent.ShowBottomNav -> showBottomNav()
            is MainUiEvent.HideBottomNav -> hideBottomNav()
        }
    }

    private fun navigateTo(route: String) {
        _uiState.update { currentState ->
            // If navigating to a bottom nav item, clear the stack and start fresh
            if (route in bottomNavRoutes) {
                currentState.copy(
                    currentRoute = route,
                    backStack = listOf(route).toImmutableList()
                )
            } else {
                // For other routes, push to stack
                val newStack = if (currentState.backStack.lastOrNull() != route) {
                    (currentState.backStack + route).toImmutableList()
                } else {
                    // Don't add duplicate if already at this route
                    currentState.backStack
                }
                currentState.copy(
                    currentRoute = route,
                    backStack = newStack
                )
            }
        }

        // Auto-hide bottom nav when navigating to detail screens
        // Show when navigating to main bottom nav screens
        if (route in bottomNavRoutes) {
            showBottomNav()
        } else {
            hideBottomNav()
        }
    }

    private fun navigateBack() {
        _uiState.update { currentState ->
            val currentStack = currentState.backStack

            // If only one item in stack (at root), can't go back
            if (currentStack.size <= 1) {
                return@update currentState
            }

            // Pop current route and navigate to previous
            val newStack = currentStack.dropLast(1).toImmutableList()
            val previousRoute = newStack.last()

            currentState.copy(
                currentRoute = previousRoute,
                backStack = newStack
            )
        }
    }

    fun canNavigateBack(): Boolean {
        return _uiState.value.backStack.size > 1
    }

    private fun showBottomNav() {
        isBottomNavVisible = true
    }

    private fun hideBottomNav() {
        isBottomNavVisible = false
    }
}
