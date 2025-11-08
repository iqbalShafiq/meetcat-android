package id.usecase.meetcat.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import id.usecase.meetcat.presentation.screen.auth.forgotpassword.ForgotPasswordScreen
import id.usecase.meetcat.presentation.screen.auth.forgotpassword.ForgotPasswordViewModel
import id.usecase.meetcat.presentation.screen.auth.login.LoginScreen
import id.usecase.meetcat.presentation.screen.auth.login.LoginViewModel
import id.usecase.meetcat.presentation.screen.auth.register.RegisterScreen
import id.usecase.meetcat.presentation.screen.auth.register.RegisterViewModel
import id.usecase.meetcat.presentation.screen.auth.splash.SplashScreen
import id.usecase.meetcat.presentation.screen.auth.splash.SplashViewModel
import id.usecase.meetcat.presentation.screen.main.MainScreen
import org.koin.androidx.compose.koinViewModel

sealed class AppRoute {
    data object Splash : AppRoute()
    data object Login : AppRoute()
    data object Register : AppRoute()
    data object ForgotPassword : AppRoute()
    data object Main : AppRoute()
}

@Composable
fun AppRouter(
    modifier: Modifier = Modifier
) {
    // Skip login for development - go directly to Main
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Main) }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            // Determine navigation direction based on route hierarchy
            val isBackNavigation = when {
                // Main -> Login/Register/ForgotPassword is back
                targetState is AppRoute.Login && initialState is AppRoute.Main -> true
                targetState is AppRoute.Register && initialState is AppRoute.Main -> true
                // Register -> Login is back
                targetState is AppRoute.Login && initialState is AppRoute.Register -> true
                // ForgotPassword -> Login is back
                targetState is AppRoute.Login && initialState is AppRoute.ForgotPassword -> true
                else -> false
            }

            if (isBackNavigation) {
                // Back: slide from left to right
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> -fullWidth / 10 }
                    ) togetherWith
                    fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> fullWidth / 10 }
                    )
            } else {
                // Forward: slide from right to left
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> fullWidth / 10 }
                    ) togetherWith
                    fadeOut(animationSpec = tween(300)) +
                    slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> -fullWidth / 10 }
                    )
            }
        },
        label = "AppRouterTransition"
    ) { route ->
        when (route) {
            is AppRoute.Splash -> {
                val viewModel: SplashViewModel = koinViewModel()
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateToMain = { currentRoute = AppRoute.Main },
                    onNavigateToLogin = { currentRoute = AppRoute.Login },
                    modifier = modifier
                )
            }

            is AppRoute.Login -> {
                val viewModel: LoginViewModel = koinViewModel()
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToMain = { currentRoute = AppRoute.Main },
                    onNavigateToRegister = { currentRoute = AppRoute.Register },
                    onNavigateToForgotPassword = { currentRoute = AppRoute.ForgotPassword },
                    modifier = modifier
                )
            }

            is AppRoute.Register -> {
                val viewModel: RegisterViewModel = koinViewModel()
                RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToMain = { currentRoute = AppRoute.Main },
                    onNavigateToLogin = { currentRoute = AppRoute.Login },
                    modifier = modifier
                )
            }

            is AppRoute.ForgotPassword -> {
                val viewModel: ForgotPasswordViewModel = koinViewModel()
                ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentRoute = AppRoute.Login },
                    modifier = modifier
                )
            }

            is AppRoute.Main -> {
                MainScreen(modifier = modifier)
            }
        }
    }
}
