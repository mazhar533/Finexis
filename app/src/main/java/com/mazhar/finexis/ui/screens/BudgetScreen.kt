package com.mazhar.finexis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.ui.theme.*
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import com.mazhar.finexis.viewmodel.BudgetViewModel
import com.mazhar.finexis.ui.components.BudgetDialog
import com.mazhar.finexis.ui.components.FadeInSlideUp
import com.mazhar.finexis.ui.components.StaggeredItem
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

data class CategoryBudget(
    val categoryName: String,
    val spent: Double,
    val totalLimit: Double,
    val indicatorColor: Color
)

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
    currency: String = "PKR",
    onLimitsPositioned: (Rect) -> Unit = {}
) {
    val expenses by viewModel.expenses.collectAsState()
    val budgetState by budgetViewModel.budget.collectAsState()

    val budgetSpent = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val budgetTotal = budgetState.monthlyLimit

    val categoryBudgets = remember(expenses, budgetState) {
        listOf(
            CategoryBudget("Food", expenses.filter { !it.isIncome && it.category.lowercase() == "food" }.sumOf { it.amount }, budgetState.foodLimit, Color(0xFFF59E0B)),
            CategoryBudget("Transport", expenses.filter { !it.isIncome && it.category.lowercase() == "transport" }.sumOf { it.amount }, budgetState.transportLimit, Color(0xFF3B82F6)),
            CategoryBudget("Shopping", expenses.filter { !it.isIncome && it.category.lowercase() == "shopping" }.sumOf { it.amount }, budgetState.shoppingLimit, Color(0xFFEC4899)),
            CategoryBudget("Other", expenses.filter { !it.isIncome && it.category.lowercase() == "other" }.sumOf { it.amount }, budgetState.otherLimit, Color(0xFF8B5CF6))
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }

    BudgetScreenContent(
        budgetSpent = budgetSpent,
        budgetTotal = budgetTotal,
        categories = categoryBudgets,
        currency = currency,
        modifier = modifier,
        onEditBudget = { showEditDialog = true },
        onLimitsPositioned = onLimitsPositioned
    )

    if (showEditDialog) {
        BudgetDialog(
            currency = currency,
            currentBudget = budgetState,
            onDismiss = { showEditDialog = false },
            onConfirm = { monthlyLimit, foodLimit, transportLimit, shoppingLimit, otherLimit ->
                budgetViewModel.saveBudget(
                    monthlyLimit = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(monthlyLimit, currency),
                    foodLimit = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(foodLimit, currency),
                    transportLimit = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(transportLimit, currency),
                    shoppingLimit = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(shoppingLimit, currency),
                    otherLimit = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(otherLimit, currency)
                )
                showEditDialog = false
            }
        )
    }
}

@Composable
fun BudgetScreenContent(
    budgetSpent: Double,
    budgetTotal: Double,
    categories: List<CategoryBudget>,
    currency: String,
    modifier: Modifier = Modifier,
    onEditBudget: () -> Unit = {},
    onLimitsPositioned: (Rect) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
    ) {

        // Header Section
        FadeInSlideUp(delayMillis = 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Circular Edit Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { onEditBudget() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Budgets",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Monthly Budget Dark Gradient Card
        val cardGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
        FadeInSlideUp(delayMillis = 100) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInRoot()
                        val size = layoutCoordinates.size
                        onLimitsPositioned(Rect(position.x, position.y, position.x + size.width, position.y + size.height))
                    }
                    .padding(bottom = 28.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .background(cardGradient)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Monthly Budget",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(budgetSpent, currency),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " / " + com.mazhar.finexis.ui.utils.CurrencyHelper.format(budgetTotal, currency),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress Bar
                    val percentUsed = if (budgetTotal > 0) (budgetSpent / budgetTotal) else 0.0
                    val percentInt = (percentUsed * 100).toInt()
                    val budgetLeft = budgetTotal - budgetSpent

                    LinearProgressIndicator(
                        progress = { percentUsed.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FinexisPrimary,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$percentInt% used",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )

                        Text(
                            text = "${com.mazhar.finexis.ui.utils.CurrencyHelper.format(budgetLeft, currency)} left",
                            color = FinexisPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Category Tracking Section Header
        FadeInSlideUp(delayMillis = 180) {
            Text(
                text = "Category Tracking",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Categories list
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEachIndexed { index, category ->
                StaggeredItem(index = index + 3) {
                    CategoryBudgetRow(category = category, currency = currency)
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetRow(category: CategoryBudget, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(category.indicatorColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.categoryName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(category.spent, currency),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " / " + com.mazhar.finexis.ui.utils.CurrencyHelper.format(category.totalLimit, currency),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val ratio = if (category.totalLimit > 0) (category.spent / category.totalLimit) else 0.0
            LinearProgressIndicator(
                progress = { ratio.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = category.indicatorColor,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenLightPreview() {
    FinexisTheme(darkTheme = false) {
        BudgetScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenDarkPreview() {
    FinexisTheme(darkTheme = true) {
        BudgetScreen()
    }
}
