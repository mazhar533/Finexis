package com.mazhar.finexis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.R
import com.mazhar.finexis.ui.theme.*

import com.mazhar.finexis.model.Expense
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val expenses by viewModel.expenses.collectAsState()

    val userName = "Mazharalihaider4"
    val incomeAmount = expenses.filter { it.isIncome }.sumOf { it.amount }
    val expenseAmount = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val totalBalance = incomeAmount - expenseAmount

    val budgetSpent = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val budgetTotal = 27800.0

    HomeScreenContent(
        userName = userName,
        totalBalance = totalBalance,
        incomeAmount = incomeAmount,
        expenseAmount = expenseAmount,
        budgetSpent = budgetSpent,
        budgetTotal = budgetTotal,
        expenses = expenses.take(5),
        modifier = modifier,
        onSettingsClick = onNavigateToSettings
    )
}

@Composable
fun HomeScreenContent(
    userName: String,
    totalBalance: Double,
    incomeAmount: Double,
    expenseAmount: Double,
    budgetSpent: Double,
    budgetTotal: Double,
    expenses: List<Expense>,
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular profile image placeholder with avatar-like appearance
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Good morning,",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Circular Settings/Icon Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_setting),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Total Balance Gradient Card
        val cardGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF006B44), Color(0xFF004D30))
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .background(cardGradient)
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Balance",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    // Currency Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PKR",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.icon_download),
                                contentDescription = "Currency Select",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(10.dp)
                                    .rotate(180f) // arrow pointing up/down indicator rotation
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rs ${String.format("%.2f", totalBalance)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Income Area
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_download),
                                contentDescription = "Income Icon",
                                tint = FinexisIncome,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Income",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Rs ${String.format("%.2f", incomeAmount)}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Vertical Separator
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                            .align(Alignment.CenterVertically)
                    )

                    // Expenses Area
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icon_download),
                                contentDescription = "Expenses Icon",
                                tint = FinexisExpense,
                                modifier = Modifier
                                    .size(14.dp)
                                    .rotate(180f) // Pointing up for expense
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Expenses",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Rs ${String.format("%.2f", expenseAmount)}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Monthly Budget Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_puggy),
                            contentDescription = "Budget Icon",
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Monthly Budget",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Rs ${String.format("%.2f", budgetSpent)} / Rs ${String.format("%.2f", budgetTotal)}",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Progress Indicator
                val percentUsed = if (budgetTotal > 0) (budgetSpent / budgetTotal) else 0.0
                val percentInt = (percentUsed * 100).toInt()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(44.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { percentUsed.toFloat() },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF1E88E5),
                        strokeWidth = 4.dp,
                        trackColor = Color(0xFFE3F2FD)
                    )
                    Text(
                        text = "$percentInt%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Recent Transactions Section Header
        Text(
            text = "Recent Transactions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Transactions Column
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            expenses.forEach { expense ->
                TransactionRow(expense = expense)
            }
        }
    }
}

@Composable
fun TransactionRow(expense: Expense) {
    val sdf = remember { SimpleDateFormat("MM/dd/yyyy", Locale.US) }
    val dateStr = sdf.format(Date(expense.date))

    val iconResId = if (expense.isIncome) {
        when (expense.category.lowercase()) {
            "salary" -> R.drawable.icon_cash
            "freelance" -> R.drawable.icon_card
            else -> R.drawable.icon_setting
        }
    } else {
        when (expense.category.lowercase()) {
            "transport" -> R.drawable.icon_transport
            "food" -> R.drawable.icon_food
            "shopping" -> R.drawable.icon_shoping
            else -> R.drawable.icon_app
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val iconBgColor = if (expense.isIncome) Color(0xFFE8F8F5) else Color(0xFFFFECEB)
                val iconTint = if (expense.isIncome) FinexisIncome else FinexisExpense
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = expense.category,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (expense.description.isNotEmpty()) expense.description else expense.category,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Text(
                        text = expense.category,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (expense.isIncome) "+Rs ${String.format("%.2f", expense.amount)}" else "-Rs ${String.format("%.2f", expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (expense.isIncome) FinexisIncome else FinexisExpense,
                    fontSize = 15.sp
                )
                Text(
                    text = dateStr,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLightPreview() {
    FinexisTheme(darkTheme = false) {
        HomeScreenContent(
            userName = "Mazharalihaider4",
            totalBalance = 26410.0,
            incomeAmount = 27800.0,
            expenseAmount = 1390.0,
            budgetSpent = 1390.0,
            budgetTotal = 27800.0,
            expenses = listOf(
                Expense(id = "1", amount = 1390.0, category = "Transport", paymentMethod = "Card", description = "Commute", isIncome = false),
                Expense(id = "2", amount = 27800.0, category = "Salary", paymentMethod = "Cash", description = "Salary Payout", isIncome = true),
                Expense(id = "3", amount = 50.0, category = "Food", paymentMethod = "Cash", description = "Coffee", isIncome = false)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenDarkPreview() {
    FinexisTheme(darkTheme = true) {
        HomeScreenContent(
            userName = "Mazharalihaider4",
            totalBalance = 26410.0,
            incomeAmount = 27800.0,
            expenseAmount = 1390.0,
            budgetSpent = 1390.0,
            budgetTotal = 27800.0,
            expenses = listOf(
                Expense(id = "1", amount = 1390.0, category = "Transport", paymentMethod = "Card", description = "Commute", isIncome = false),
                Expense(id = "2", amount = 27800.0, category = "Salary", paymentMethod = "Cash", description = "Salary Payout", isIncome = true),
                Expense(id = "3", amount = 50.0, category = "Food", paymentMethod = "Cash", description = "Coffee", isIncome = false)
            )
        )
    }
}
