package com.mazhar.finexis.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mazhar.finexis.R
import com.mazhar.finexis.ui.theme.*
import com.mazhar.finexis.viewmodel.AuthViewModel
import com.mazhar.finexis.viewmodel.PreferenceViewModel
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.mazhar.finexis.ui.components.FadeInSlideUp
import com.mazhar.finexis.ui.components.AccountInfoBottomSheet
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MailOutline
import com.mazhar.finexis.ui.components.FaqBottomSheet
import com.mazhar.finexis.ui.components.PrivacyPolicyBottomSheet
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    onPreferencesPositioned: (Rect) -> Unit = {}
) {
    val currentUserEmail by authViewModel.currentUser.collectAsState()
    val displayNameState by authViewModel.displayName.collectAsState()
    val isVerified by authViewModel.isEmailVerified.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val context = LocalContext.current
    
    val emailToDisplay = currentUserEmail ?: "mazharalihaider4@gmail.com"
    
    val isDarkMode by preferenceViewModel.isDarkMode.collectAsState()
    val isBiometricEnabled by preferenceViewModel.isBiometricEnabled.collectAsState()
    val currency by preferenceViewModel.currency.collectAsState()

    var showAccountInfo by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showFaq by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    // Check verification status periodically when ProfileScreen starts
    LaunchedEffect(Unit) {
        authViewModel.checkEmailVerificationStatus()
    }

    Box(modifier = modifier.fillMaxSize()) {
        ProfileScreenContent(
            userName = displayNameState,
            userEmail = emailToDisplay,
            isVerified = isVerified,
            onUserCardClick = { showAccountInfo = true },
            isDarkMode = isDarkMode,
            isBiometricEnabled = isBiometricEnabled,
            currency = currency,
            onThemeToggle = { preferenceViewModel.toggleTheme() },
            onBiometricToggle = {
                if (!isBiometricEnabled) {
                    if (com.mazhar.finexis.ui.utils.BiometricHelper.isBiometricSupported(context)) {
                        preferenceViewModel.toggleBiometric()
                    } else {
                        preferenceViewModel.showToast("Touch ID is not supported or set up on this device.", true)
                    }
                } else {
                    preferenceViewModel.toggleBiometric()
                }
            },
            onCurrencyToggle = { showCurrencyDialog = true },
            onLogout = {
                authViewModel.logout()
                preferenceViewModel.showToast("Logout successful", false)
                onLogoutSuccess()
            },
            onExportPdf = {
                val incomeAmount = expenses.filter { it.isIncome }.sumOf { it.amount }
                val expenseAmount = expenses.filter { !it.isIncome }.sumOf { it.amount }
                val savingsAmount = incomeAmount - expenseAmount

                com.mazhar.finexis.ui.utils.PdfExportHelper.exportTransactionsToPdf(
                    context = context,
                    expenses = expenses,
                    currency = currency,
                    totalIncome = incomeAmount,
                    totalExpenses = expenseAmount,
                    savings = savingsAmount,
                    onSuccess = {
                        preferenceViewModel.showToast("PDF statement exported successfully!", false)
                    },
                    onError = { err ->
                        preferenceViewModel.showToast(err, true)
                    }
                )
            },
            onFaqClick = { showFaq = true },
            onPrivacyPolicyClick = { showPrivacyPolicy = true },
            onContactSupportClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:support@finexis.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "Finexis Support Request")
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    preferenceViewModel.showToast("No email client installed.", true)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onPreferencesPositioned = onPreferencesPositioned
        )
    }

    if (showAccountInfo) {
        AccountInfoBottomSheet(
            authViewModel = authViewModel,
            userName = displayNameState,
            userEmail = emailToDisplay,
            isDark = isDarkMode,
            onDismiss = { showAccountInfo = false },
            onShowToast = { msg, isErr ->
                preferenceViewModel.showToast(msg, isErr)
            }
        )
    }

    if (showFaq) {
        FaqBottomSheet(
            isDark = isDarkMode,
            onDismiss = { showFaq = false }
        )
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyBottomSheet(
            isDark = isDarkMode,
            onDismiss = { showPrivacyPolicy = false }
        )
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = {
                Text(
                    text = "Select Default Currency",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currencies = listOf(
                        "PKR" to "Pakistani Rupee (PKR)",
                        "USD" to "US Dollar (USD)",
                        "EUR" to "Euro (EUR)",
                        "GBP" to "British Pound (GBP)",
                        "INR" to "Indian Rupee (INR)",
                        "SAR" to "Saudi Riyal (SAR)",
                        "AED" to "UAE Dirham (AED)"
                    )
                    currencies.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferenceViewModel.setCurrency(code)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontSize = 15.sp,
                                color = if (currency == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (currency == code) FontWeight.Bold else FontWeight.Normal
                            )
                            if (currency == code) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
fun ProfileScreenContent(
    userName: String,
    userEmail: String,
    isVerified: Boolean,
    onUserCardClick: () -> Unit,
    isDarkMode: Boolean,
    isBiometricEnabled: Boolean,
    currency: String,
    onThemeToggle: () -> Unit,
    onBiometricToggle: () -> Unit,
    onCurrencyToggle: () -> Unit,
    onLogout: () -> Unit,
    onExportPdf: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onContactSupportClick: () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onPreferencesPositioned: (Rect) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
    ) {

        // Title
        FadeInSlideUp(delayMillis = 0) {
            Column {
                Text(
                    text = "Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage your account and settings",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }
        }

        // User Info Card
        FadeInSlideUp(delayMillis = 100) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onUserCardClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar initials box
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 26.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_verify),
                                    contentDescription = "Verified User",
                                    tint = Color(0xFF00A86B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userEmail,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Account Details",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Section: Preferences
        FadeInSlideUp(delayMillis = 180) {
            Text(
                text = "Preferences",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
        }

        FadeInSlideUp(delayMillis = 240) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInRoot()
                        val size = layoutCoordinates.size
                        onPreferencesPositioned(Rect(position.x, position.y, position.x + size.width, position.y + size.height))
                    }
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Default Currency Row
                    ProfileSettingRow(
                        iconResId = R.drawable.icon_card,
                        title = "Default Currency",
                        subtitle = when (currency) {
                            "PKR" -> "Pakistani Rupee (PKR)"
                            "USD" -> "US Dollar (USD)"
                            "EUR" -> "Euro (EUR)"
                            "GBP" -> "British Pound (GBP)"
                            "INR" -> "Indian Rupee (INR)"
                            "SAR" -> "Saudi Riyal (SAR)"
                            "AED" -> "UAE Dirham (AED)"
                            else -> currency
                        },
                        action = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    .clickable { onCurrencyToggle() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Change",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // App Theme Row
                    ProfileSettingRow(
                        iconResId = if (isDarkMode) R.drawable.icon_dark else R.drawable.icon_light,
                        title = "App Theme",
                        subtitle = if (isDarkMode) "Dark Mode" else "Light Mode",
                        action = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onThemeToggle() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    )
                }
            }
        }

        // Section: Security & Data
        FadeInSlideUp(delayMillis = 320) {
            Text(
                text = "Security & Data",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
        }

        FadeInSlideUp(delayMillis = 380) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Biometric Login Row
                    ProfileSettingRow(
                        iconResId = R.drawable.icon_biomatric,
                        title = "Touch ID Login",
                        subtitle = "Touch ID",
                        action = {
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { onBiometricToggle() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Export Statement Row
                    ProfileSettingRow(
                        iconResId = R.drawable.icon_download,
                        title = "Export Statement",
                        subtitle = "Download data as PDF",
                        action = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Navigate",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.clickable { onExportPdf() }
                    )
                }
            }
        }

        // Section: Support & Info
        FadeInSlideUp(delayMillis = 440) {
            Text(
                text = "Support & Information",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
        }

        FadeInSlideUp(delayMillis = 500) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // FAQ Row
                    ProfileSettingRow(
                        iconVector = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "FAQ & Help",
                        subtitle = "Frequently asked questions",
                        action = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open FAQ",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.clickable { onFaqClick() }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Privacy Policy Row
                    ProfileSettingRow(
                        iconVector = Icons.Default.Description,
                        title = "Privacy Policy",
                        subtitle = "Data handling guidelines",
                        action = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open Privacy Policy",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.clickable { onPrivacyPolicyClick() }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Contact Support Row
                    ProfileSettingRow(
                        iconVector = Icons.Default.MailOutline,
                        title = "Contact Support",
                        subtitle = "Get in touch with us",
                        action = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Contact Support",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.clickable { onContactSupportClick() }
                    )
                }
            }
        }

        // Log Out Button Card
        FadeInSlideUp(delayMillis = 560) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Log Out",
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log Out",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // App Version Info
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Version 1.0.0 (Build 1)",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ProfileSettingRow(
    title: String,
    subtitle: String,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (iconVector != null) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (iconResId != null) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        action()
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenLightPreview() {
    FinexisTheme(darkTheme = false) {
        ProfileScreenContent(
            userName = "Mazharalihaider4",
            userEmail = "mazharalihaider4@gmail.com",
            isVerified = true,
            onUserCardClick = {},
            isDarkMode = false,
            isBiometricEnabled = true,
            currency = "PKR",
            onThemeToggle = {},
            onBiometricToggle = {},
            onCurrencyToggle = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenDarkPreview() {
    FinexisTheme(darkTheme = true) {
        ProfileScreenContent(
            userName = "Mazharalihaider4",
            userEmail = "mazharalihaider4@gmail.com",
            isVerified = false,
            onUserCardClick = {},
            isDarkMode = true,
            isBiometricEnabled = false,
            currency = "USD",
            onThemeToggle = {},
            onBiometricToggle = {},
            onCurrencyToggle = {},
            onLogout = {}
        )
    }
}
