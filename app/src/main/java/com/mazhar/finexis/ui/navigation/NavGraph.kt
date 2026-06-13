package com.mazhar.finexis.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mazhar.finexis.ui.screens.LoginScreen
import com.mazhar.finexis.ui.screens.SignupScreen
import com.mazhar.finexis.ui.theme.FinexisBg
import com.mazhar.finexis.ui.theme.FinexisTextPrimary
import com.mazhar.finexis.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Main : Screen("main")
}

@Composable
fun FinexisNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
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
                viewModel = authViewModel
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
                viewModel = authViewModel
            )
        }
        
        composable(Screen.Main.route) {
            // Temporary main content screen, to be replaced by the bottom nav container later
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FinexisBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome to Finexis Main Screen!",
                    color = FinexisTextPrimary,
                    fontSize = 20.sp
                )
            }
        }
    }
}
