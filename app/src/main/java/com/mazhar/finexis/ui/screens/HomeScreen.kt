package com.mazhar.finexis.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import com.mazhar.finexis.R
import com.mazhar.finexis.ui.theme.*

import com.mazhar.finexis.model.Expense
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import com.mazhar.finexis.viewmodel.BudgetViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mazhar.finexis.ui.components.FadeInSlideUp
import com.mazhar.finexis.ui.components.StaggeredItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val expenses by viewModel.expenses.collectAsState()
    val budgetState by budgetViewModel.budget.collectAsState()

    val userName = "Mazharalihaider4"
    val incomeAmount = expenses.filter { it.isIncome }.sumOf { it.amount }
    val expenseAmount = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val totalBalance = incomeAmount - expenseAmount

    val budgetSpent = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val budgetTotal = budgetState.monthlyLimit

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

@SuppressLint("DefaultLocale")
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
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
    ) {

        // App Brand Top Bar
        FadeInSlideUp(delayMillis = 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_app),
                            contentDescription = "Finexis Logo",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Finexis",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Settings Button in Brand Bar
                Box(
                    modifier = Modifier
                        .size(40.dp)
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
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Greeting Row Below Brand Bar
        FadeInSlideUp(delayMillis = 50) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular profile image placeholder with avatar-like appearance
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Good morning,",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Total Balance Card
        FadeInSlideUp(delayMillis = 100) {
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
                                    painter = painterResource(id = R.drawable.icon_arrow_down),
                                    contentDescription = "Currency Select",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(10.dp)
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
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_arrow),
                                    contentDescription = "Income Icon",
                                    tint = FinexisIncome,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .rotate(0f) // Down-right default
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Income",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rs ${String.format("%.2f", incomeAmount)}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_arrow),
                                    contentDescription = "Expenses Icon",
                                    tint = FinexisExpense,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .rotate(270f) // Top-right
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Expenses",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
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
        FadeInSlideUp(delayMillis = 200) {
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
        }

        // Recent Transactions Section Header
        FadeInSlideUp(delayMillis = 300) {
            Text(
                text = "Recent Transactions",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Transactions Column
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            expenses.forEachIndexed { index, expense ->
                StaggeredItem(index = index + 5) {
                    TransactionRow(expense = expense)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
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
                        text = expense.description.ifEmpty { expense.category },
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
