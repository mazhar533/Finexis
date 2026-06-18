package com.mazhar.finexis.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.ui.theme.FinexisPrimary

data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqBottomSheet(
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val faqList = remember {
        listOf(
            FaqItem(
                question = "How does Finexis store my transaction data?",
                answer = "All your transactions and budget configurations are synced securely in real-time with Firebase Firestore. Your personal information is encrypted and never shared with third parties."
            ),
            FaqItem(
                question = "How does the dynamic exchange rate work?",
                answer = "Finexis automatically fetches live conversion rates from an open-access currency exchange CDN on startup. You can switch currencies on the Home screen to view your transactions in your preferred currency instantly."
            ),
            FaqItem(
                question = "What happens if I exceed my monthly budget?",
                answer = "Finexis keeps track of your category and overall spending. When your expenses reach 90% or 100% of your limits, the app automatically triggers a local notification warning so you stay within your budget."
            ),
            FaqItem(
                question = "Is biometric login secure?",
                answer = "Yes, biometric authentication runs locally on your device's secure hardware enclave. Finexis does not store your fingerprint or face data; it only requests confirmation from the Android operating system."
            )
        )
    }

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
                text = "FAQ & Help",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                faqList.forEach { item ->
                    var isExpanded by remember { mutableStateOf(false) }

                    val cardColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                    val borderColor = if (isExpanded) FinexisPrimary.copy(alpha = 0.5f) else Color.Transparent

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable { isExpanded = !isExpanded }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.question,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle answer",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = item.answer,
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
