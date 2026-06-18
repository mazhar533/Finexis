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
import com.mazhar.finexis.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mazhar.finexis.ui.components.FadeInSlideUp
import com.mazhar.finexis.ui.components.StaggeredItem
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.platform.LocalContext
import com.mazhar.finexis.notification.NotificationHelper
import com.mazhar.finexis.ui.components.NotificationBottomSheet

@SuppressLint("AutoboxingStateCreation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    currency: String = "PKR",
    onCurrencyChange: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNotificationPositioned: (Rect) -> Unit = {},
    onBalancePositioned: (Rect) -> Unit = {},
    onBudgetPositioned: (Rect) -> Unit = {}
) {
    val expenses by viewModel.expenses.collectAsState()
    val budgetState by budgetViewModel.budget.collectAsState()
    val displayName by authViewModel.displayName.collectAsState()
    val context = LocalContext.current

    var unreadCount by remember { mutableIntStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(expenses, budgetState, showNotifications) {
        unreadCount = NotificationHelper.getNotifications(context).count { !it.isRead }
    }

    val userName = displayName
    val incomeAmount = expenses.filter { it.isIncome }.sumOf { it.amount }
    val expenseAmount = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val totalBalance = incomeAmount - expenseAmount

    val budgetSpent = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val budgetTotal = budgetState.monthlyLimit

    Box(modifier = Modifier.fillMaxSize()) {
        HomeScreenContent(
            userName = userName,
            totalBalance = totalBalance,
            incomeAmount = incomeAmount,
            expenseAmount = expenseAmount,
            budgetSpent = budgetSpent,
            budgetTotal = budgetTotal,
            expenses = expenses.take(5),
            currency = currency,
            unreadCount = unreadCount,
            onNotificationClick = { showNotifications = true },
            onCurrencyClick = { showCurrencyDialog = true },
            onNotificationPositioned = onNotificationPositioned,
            onBalancePositioned = onBalancePositioned,
            onBudgetPositioned = onBudgetPositioned,
            modifier = modifier,
            onSettingsClick = onNavigateToSettings
        )
    }

    if (showNotifications) {
        NotificationBottomSheet(
            onDismiss = { showNotifications = false }
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
                                    onCurrencyChange(code)
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
    currency: String,
    unreadCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onCurrencyClick: () -> Unit = {},
    onNotificationPositioned: (Rect) -> Unit = {},
    onBalancePositioned: (Rect) -> Unit = {},
    onBudgetPositioned: (Rect) -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            in 17..20 -> "Good evening,"
            else -> "Good night,"
        }
    }

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Notification Bell Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .onGloballyPositioned { layoutCoordinates ->
                                val position = layoutCoordinates.positionInRoot()
                                val size = layoutCoordinates.size
                                onNotificationPositioned(Rect(position.x, position.y, position.x + size.width, position.y + size.height))
                            }
                            .clickable { onNotificationClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 8.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    // Settings Button
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
                        text = greeting,
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
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInRoot()
                        val size = layoutCoordinates.size
                        onBalancePositioned(Rect(position.x, position.y, position.x + size.width, position.y + size.height))
                    }
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
                                .clickable { onCurrencyClick() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currency,
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
                        text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(totalBalance, currency, showDecimal = true),
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
                                text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(incomeAmount, currency, showDecimal = true),
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
                                text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(expenseAmount, currency, showDecimal = true),
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
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInRoot()
                        val size = layoutCoordinates.size
                        onBudgetPositioned(Rect(position.x, position.y, position.x + size.width, position.y + size.height))
                    }
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
                                painter = painterResource(id = R.drawable.icon_coin),
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
                                text = "${com.mazhar.finexis.ui.utils.CurrencyHelper.format(budgetSpent, currency, showDecimal = true)} / ${com.mazhar.finexis.ui.utils.CurrencyHelper.format(budgetTotal, currency, showDecimal = true)}",
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
                    TransactionRow(expense = expense, currency = currency)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TransactionRow(expense: Expense, currency: String) {
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
                    text = com.mazhar.finexis.ui.utils.CurrencyHelper.formatSigned(expense.amount, currency, expense.isIncome, showDecimal = true),
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
            ),
            currency = "PKR"
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
            ),
            currency = "USD"
        )
    }
}
