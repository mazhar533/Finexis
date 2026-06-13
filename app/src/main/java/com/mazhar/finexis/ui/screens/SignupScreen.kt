package com.mazhar.finexis.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
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
import com.mazhar.finexis.ui.theme.FinexisBg
import com.mazhar.finexis.ui.theme.FinexisBorder
import com.mazhar.finexis.ui.theme.FinexisPrimary
import com.mazhar.finexis.ui.theme.FinexisSurface
import com.mazhar.finexis.ui.theme.FinexisTextPrimary
import com.mazhar.finexis.ui.theme.FinexisTextSecondary
import com.mazhar.finexis.ui.theme.FinexisTheme
import com.mazhar.finexis.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinexisBg)
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
            color = FinexisTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Secure, premium wealth management.",
            fontSize = 16.sp,
            color = FinexisTextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Form Fields
        FinexisTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            placeholder = "hello@example.com",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        FinexisTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "••••••••",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Action Button
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FinexisPrimary)
            }
        } else {
            FinexisButton(
                text = "Create Account",
                showArrow = true,
                onClick = {
                    viewModel.signUp(email, password, onSignupSuccess)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Separator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = FinexisBorder
            )
            Text(
                text = "Or continue with",
                color = FinexisTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = FinexisBorder
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Biometric Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(FinexisSurface)
                    .border(1.dp, FinexisBorder, CircleShape)
                    .clickable {
                        Toast.makeText(context, "Biometric login simulated", Toast.LENGTH_SHORT).show()
                        viewModel.login("biometric@finexis.com", "biometric_pass", onSignupSuccess)
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
                color = FinexisTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        // Login Footer Navigation
        val footerText = buildAnnotatedString {
            append("Already have an account? ")
            withStyle(style = SpanStyle(color = FinexisPrimary, fontWeight = FontWeight.Bold)) {
                append("Sign in")
            }
        }

        Text(
            text = footerText,
            color = FinexisTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToLogin() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    FinexisTheme {
        SignupScreen(
            onSignupSuccess = {},
            onNavigateToLogin = {}
        )
    }
}
