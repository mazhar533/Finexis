package com.mazhar.finexis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.R
import com.mazhar.finexis.model.Budget
import com.mazhar.finexis.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDialog(
    currentBudget: Budget,
    onDismiss: () -> Unit,
    onConfirm: (monthlyLimit: Double, foodLimit: Double, transportLimit: Double, shoppingLimit: Double, otherLimit: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form States
    var monthlyLimit by remember { mutableStateOf(currentBudget.monthlyLimit.toString()) }
    var foodLimit by remember { mutableStateOf(currentBudget.foodLimit.toString()) }
    var transportLimit by remember { mutableStateOf(currentBudget.transportLimit.toString()) }
    var shoppingLimit by remember { mutableStateOf(currentBudget.shoppingLimit.toString()) }
    var otherLimit by remember { mutableStateOf(currentBudget.otherLimit.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Circular Dismiss button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Budget Input Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Monthly Budget Limit",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = monthlyLimit,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.toDoubleOrNull() != null) {
                            monthlyLimit = input
                        }
                    },
                    placeholder = { Text("Enter monthly limit", color = MaterialTheme.colorScheme.outline) },
                    prefix = { Text("Rs ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(20.dp))

            // Category Limits Header
            Text(
                text = "Category Budget Limits",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Category Budget Input Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Food Limit
                CategoryBudgetField(
                    label = "Food Limit",
                    iconResId = R.drawable.icon_food,
                    iconTint = Color(0xFFF59E0B),
                    value = foodLimit,
                    onValueChange = { foodLimit = it }
                )

                // Transport Limit
                CategoryBudgetField(
                    label = "Transport Limit",
                    iconResId = R.drawable.icon_transport,
                    iconTint = Color(0xFF3B82F6),
                    value = transportLimit,
                    onValueChange = { transportLimit = it }
                )

                // Shopping Limit
                CategoryBudgetField(
                    label = "Shopping Limit",
                    iconResId = R.drawable.icon_shoping,
                    iconTint = Color(0xFFEC4899),
                    value = shoppingLimit,
                    onValueChange = { shoppingLimit = it }
                )

                // Other Limit
                CategoryBudgetField(
                    label = "Other Limit",
                    iconResId = R.drawable.icon_app,
                    iconTint = Color(0xFF8B5CF6),
                    value = otherLimit,
                    onValueChange = { otherLimit = it }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Confirm Save Button
            Button(
                onClick = {
                    val mLimit = monthlyLimit.toDoubleOrNull() ?: 0.0
                    val fLimit = foodLimit.toDoubleOrNull() ?: 0.0
                    val tLimit = transportLimit.toDoubleOrNull() ?: 0.0
                    val sLimit = shoppingLimit.toDoubleOrNull() ?: 0.0
                    val oLimit = otherLimit.toDoubleOrNull() ?: 0.0

                    if (mLimit > 0) {
                        onConfirm(mLimit, fLimit, tLimit, sLimit, oLimit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinexisPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Budgets",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetField(
    label: String,
    iconResId: Int,
    iconTint: Color,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (input.isEmpty() || input.toDoubleOrNull() != null) {
                    onValueChange(input)
                }
            },
            placeholder = { Text("0.00", color = MaterialTheme.colorScheme.outline) },
            prefix = { Text("Rs ", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary) },
            modifier = Modifier.width(140.dp),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetBottomSheetLightPreview() {
    FinexisTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            BudgetDialog(
                currentBudget = Budget(),
                onDismiss = {},
                onConfirm = { _, _, _, _, _ -> }
            )
        }
    }
}
