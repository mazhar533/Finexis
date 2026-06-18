package com.mazhar.finexis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyBottomSheet(
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.background,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            Text(
                text = "Privacy Policy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1
                PrivacySection(
                    title = "1. Data Collection",
                    content = "We collect your email address, display name, and financial transactions (amount, category, payment method, description, date) solely to provide real-time budget synchronization and personal finance calculations.",
                    isDark = isDark
                )

                // Section 2
                PrivacySection(
                    title = "2. Data Security & Storage",
                    content = "All transaction data is stored securely in Firebase Firestore under industry-standard encryption protocols. Your data belongs entirely to you and we implement security rules to prevent unauthorized access.",
                    isDark = isDark
                )

                // Section 3
                PrivacySection(
                    title = "3. Third-Party Services",
                    content = "We utilize FawazAhmed's open currency exchange rates API for live exchange rate calculations. This CDN is public and anonymous; Finexis does not share any user or transaction data with this service.",
                    isDark = isDark
                )

                // Section 4
                PrivacySection(
                    title = "4. User Consent & Rights",
                    content = "By using Finexis, you consent to the storage and local processing of your preferences and biometrics. You can toggle off biometric credentials or request complete account and data erasure via the support team.",
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun PrivacySection(
    title: String,
    content: String,
    isDark: Boolean
) {
    val cardColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}
