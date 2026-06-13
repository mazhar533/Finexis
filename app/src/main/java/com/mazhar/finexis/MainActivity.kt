package com.mazhar.finexis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mazhar.finexis.ui.navigation.FinexisNavGraph
import com.mazhar.finexis.ui.navigation.Screen
import com.mazhar.finexis.ui.theme.FinexisTheme
import com.mazhar.finexis.viewmodel.AuthViewModel
import com.mazhar.finexis.viewmodel.PreferenceViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val preferenceViewModel: PreferenceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by preferenceViewModel.isDarkMode.collectAsState()
            
            FinexisTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                
                // Persistent session check: if currentUser is not null, go directly to Main
                val currentUserEmail = authViewModel.currentUser.value
                val startDestination = if (currentUserEmail != null) {
                    Screen.Main.route
                } else {
                    Screen.Login.route
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FinexisNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}