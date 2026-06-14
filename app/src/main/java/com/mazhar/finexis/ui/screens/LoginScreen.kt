package com.mazhar.finexis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mazhar.finexis.R
import com.mazhar.finexis.ui.components.FinexisButton
import com.mazhar.finexis.ui.components.FinexisTextField
import com.mazhar.finexis.ui.theme.FinexisPrimary
import com.mazhar.finexis.ui.theme.FinexisTheme
import com.mazhar.finexis.viewmodel.AuthViewModel
import com.mazhar.finexis.viewmodel.PreferenceViewModel
import com.mazhar.finexis.ui.components.ForgotPasswordBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isDarkMode by preferenceViewModel.isDarkMode.collectAsState()
    val isBiometricEnabled by preferenceViewModel.isBiometricEnabled.collectAsState()
    val context = LocalContext.current

    LoginScreenContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        isDarkMode = isDarkMode,
        isBiometricEnabled = isBiometricEnabled,
        onLoginClick = { email, password ->
            viewModel.login(email, password) {
                if (isBiometricEnabled) {
                    preferenceViewModel.saveCachedCredentials(email, password)
                }
                onLoginSuccess()
            }
        },
        onForgotPasswordClick = { email, callback ->
            viewModel.sendPasswordResetEmail(email, callback)
        },
        onNavigateToSignup = onNavigateToSignup,
        clearError = { viewModel.clearError() },
        onBiometricClick = {
            if (!isBiometricEnabled) {
                Toast.makeText(
                    context,
                    "Please enable Biometric Login in Profile Settings first.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val credentials = preferenceViewModel.getCachedCredentials()
                if (credentials == null) {
                    Toast.makeText(
                        context,
                        "Please sign in manually with email and password once to configure biometric credentials.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val activity = com.mazhar.finexis.ui.utils.BiometricHelper.findActivity(context)
                    if (activity == null) {
                        Toast.makeText(context, "Could not launch biometric prompt: host activity not found.", Toast.LENGTH_LONG).show()
                    } else {
                        com.mazhar.finexis.ui.utils.BiometricHelper.showBiometricPrompt(
                            activity = activity,
                            onSuccess = {
                                viewModel.login(credentials.first, credentials.second) {
                                    onLoginSuccess()
                                }
                            },
                            onError = { err ->
                                Toast.makeText(context, "Authentication failed: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    isDarkMode: Boolean,
    isBiometricEnabled: Boolean,
    onLoginClick: (String, String) -> Unit,
    onForgotPasswordClick: (String, (Boolean, String) -> Unit) -> Unit,
    onNavigateToSignup: () -> Unit,
    clearError: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPasswordSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            clearError()
        }
    }

    if (showForgotPasswordSheet) {
        ForgotPasswordBottomSheet(
            initialEmail = email,
            isDark = isDarkMode,
            onDismiss = { showForgotPasswordSheet = false },
            onSendResetLink = onForgotPasswordClick
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // App Logo
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(FinexisPrimary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_app),
                contentDescription = "Finexis Logo",
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Finexis",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Secure, premium wealth management.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Form Fields
        FinexisTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            placeholder = "name@domain.com",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(20.dp))

        FinexisTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Enter password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Forgot Password clickable link
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Forgot Password?",
                color = FinexisPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    showForgotPasswordSheet = true
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button or Loading Indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FinexisPrimary)
            }
        } else {
            FinexisButton(
                text = "Sign In",
                onClick = { onLoginClick(email, password) },
                showArrow = true,
                enabled = email.isNotEmpty() && password.isNotEmpty()
            )
        }

        if (isBiometricEnabled) {
            Spacer(modifier = Modifier.height(32.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Text(
                    text = "OR",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Biometric Button Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable {
                            onBiometricClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_biomatric),
                        contentDescription = "Biometric Login",
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Biometric Login",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        // Sign Up Footer Navigation
        val footerText = buildAnnotatedString {
            append("Don't have an account? ")
            withStyle(style = SpanStyle(color = FinexisPrimary, fontWeight = FontWeight.Bold)) {
                append("Sign up")
            }
        }

        Text(
            text = footerText,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSignup() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FinexisTheme {
        LoginScreenContent(
            isLoading = false,
            errorMessage = null,
            isDarkMode = false,
            isBiometricEnabled = true,
            onLoginClick = { _, _ -> },
            onForgotPasswordClick = { _, _ -> },
            onNavigateToSignup = {},
            clearError = {},
            onBiometricClick = {}
        )
    }
}
