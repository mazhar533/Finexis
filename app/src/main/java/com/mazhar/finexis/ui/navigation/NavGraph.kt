package com.mazhar.finexis.ui.navigation


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mazhar.finexis.ui.screens.LoginScreen
import com.mazhar.finexis.ui.screens.SignupScreen
import com.mazhar.finexis.ui.screens.MainContainerScreen
import com.mazhar.finexis.ui.screens.SplashScreen
import com.mazhar.finexis.ui.screens.OnboardingScreen
import com.mazhar.finexis.viewmodel.AuthViewModel
import com.mazhar.finexis.viewmodel.PreferenceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Main : Screen("main")
}

@Composable
fun FinexisNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String,
    preferenceViewModel: PreferenceViewModel = viewModel(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                preferenceViewModel = preferenceViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                preferenceViewModel = preferenceViewModel
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
                viewModel = authViewModel,
                preferenceViewModel = preferenceViewModel
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
                viewModel = authViewModel,
                preferenceViewModel = preferenceViewModel
            )
        }
        
        composable(Screen.Main.route) {
            MainContainerScreen(
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                preferenceViewModel = preferenceViewModel,
                authViewModel = authViewModel
            )
        }
    }
}
