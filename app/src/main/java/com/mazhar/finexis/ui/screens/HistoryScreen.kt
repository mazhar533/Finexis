package com.mazhar.finexis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.mazhar.finexis.R
import com.mazhar.finexis.ui.theme.*

import com.mazhar.finexis.model.Expense
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import com.mazhar.finexis.ui.components.ExpenseDialog
import com.mazhar.finexis.ui.components.FadeInSlideUp
import com.mazhar.finexis.ui.components.StaggeredItem
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(),
    currency: String = "PKR"
) {
    val expenses by viewModel.expenses.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedDateFilter by remember { mutableStateOf("All Time") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    HistoryScreenContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedFilter = selectedFilter,
        onFilterSelect = { selectedFilter = it },
        selectedDateFilter = selectedDateFilter,
        onDateFilterSelect = { selectedDateFilter = it },
        selectedCategoryFilter = selectedCategoryFilter,
        onCategoryFilterSelect = { selectedCategoryFilter = it },
        expenses = expenses,
        currency = currency,
        onDeleteTransaction = { id -> viewModel.deleteExpense(id) },
        onEditTransaction = { expense -> expenseToEdit = expense },
        modifier = modifier
    )

    if (expenseToEdit != null) {
        ExpenseDialog(
            currency = currency,
            expenseToEdit = expenseToEdit,
            onDismiss = { expenseToEdit = null },
            onConfirm = { amount, category, date, paymentMethod, description, isIncome ->
                val amountInPkr = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(amount, currency)
                viewModel.editExpense(
                    id = expenseToEdit!!.id,
                    amount = amountInPkr,
                    category = category,
                    date = date,
                    paymentMethod = paymentMethod,
                    description = description,
                    isIncome = isIncome
                )
                expenseToEdit = null
            }
        )
    }
}

@Composable
fun HistoryScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelect: (String) -> Unit,
    selectedDateFilter: String,
    onDateFilterSelect: (String) -> Unit,
    selectedCategoryFilter: String,
    onCategoryFilterSelect: (String) -> Unit,
    expenses: List<Expense>,
    currency: String,
    onDeleteTransaction: (String) -> Unit,
    onEditTransaction: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Consume horizontal scroll to prevent page swiping on boundaries
                return Offset(x = available.x, y = 0f)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
    ) {

        // Title and Search Bar
        FadeInSlideUp(delayMillis = 0) {
            Column {
                Text(
                    text = "History",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search transactions...", color = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
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

        // Filtering Section: Type, Date, Category
        Column(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .nestedScroll(filterScrollConnection),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Filter by Type
            FadeInSlideUp(delayMillis = 80) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Type:",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(75.dp)
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "Expense", "Income")
                        filters.forEach { filter ->
                            val isActive = selectedFilter == filter
                            val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            val borderColor = if (isActive) Color.Transparent else MaterialTheme.colorScheme.outline

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable { onFilterSelect(filter) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Filter by Date Range
            FadeInSlideUp(delayMillis = 120) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Date:",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(75.dp)
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val dateFilters = listOf("All Time", "Today", "This Week", "This Month")
                        dateFilters.forEach { filter ->
                            val isActive = selectedDateFilter == filter
                            val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            val borderColor = if (isActive) Color.Transparent else MaterialTheme.colorScheme.outline

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable { onDateFilterSelect(filter) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Filter by Category
            FadeInSlideUp(delayMillis = 160) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Category:",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(75.dp)
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categoryFilters = listOf("All", "Salary", "Food", "Transport", "Shopping", "Other")
                        categoryFilters.forEach { filter ->
                            val isActive = selectedCategoryFilter == filter
                            val bgColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val textColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            val borderColor = if (isActive) Color.Transparent else MaterialTheme.colorScheme.outline

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable { onCategoryFilterSelect(filter) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Transactions list
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filteredExpenses = expenses.filter {
                val matchesSearch = it.category.contains(searchQuery, ignoreCase = true) || 
                                    it.description.contains(searchQuery, ignoreCase = true)
                
                val matchesFilter = when (selectedFilter) {
                    "Expense" -> !it.isIncome
                    "Income" -> it.isIncome
                    else -> true
                }

                val matchesDate = when (selectedDateFilter) {
                    "Today" -> {
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        it.date >= today
                    }
                    "This Week" -> {
                        val thisWeek = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        it.date >= thisWeek
                    }
                    "This Month" -> {
                        val thisMonth = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        it.date >= thisMonth
                    }
                    else -> true
                }

                val matchesCategory = when (selectedCategoryFilter) {
                    "All" -> true
                    else -> it.category.equals(selectedCategoryFilter, ignoreCase = true)
                }

                matchesSearch && matchesFilter && matchesDate && matchesCategory
            }

            filteredExpenses.forEachIndexed { index, expense ->
                StaggeredItem(index = index + 5) {
                    HistoryTransactionRow(
                        expense = expense,
                        currency = currency,
                        onDelete = { onDeleteTransaction(expense.id) },
                        onEdit = { onEditTransaction(expense) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryTransactionRow(
    expense: Expense,
    currency: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MM/dd/yyyy", Locale.US) }
    val dateStr = sdf.format(Date(expense.date))
    val subtitleText = "$dateStr • ${expense.paymentMethod.lowercase()}"

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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onEdit() },
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
                        text = subtitleText,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 13.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = com.mazhar.finexis.ui.utils.CurrencyHelper.formatSigned(expense.amount, currency, expense.isIncome, showDecimal = true),
                    fontWeight = FontWeight.Bold,
                    color = if (expense.isIncome) FinexisIncome else FinexisExpense,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenLightPreview() {
    FinexisTheme(darkTheme = false) {
        HistoryScreenContent(
            searchQuery = "",
            onSearchQueryChange = {},
            selectedFilter = "All",
            onFilterSelect = {},
            selectedDateFilter = "All Time",
            onDateFilterSelect = {},
            selectedCategoryFilter = "All",
            onCategoryFilterSelect = {},
            expenses = listOf(
                Expense(id = "1", amount = 1390.0, category = "Transport", paymentMethod = "Card", description = "Daily commute", isIncome = false),
                Expense(id = "2", amount = 27800.0, category = "Salary", paymentMethod = "Cash", description = "Monthly salary payout", isIncome = true),
                Expense(id = "3", amount = 50.0, category = "Food", paymentMethod = "Cash", description = "Coffee", isIncome = false)
            ),
            currency = "PKR",
            onDeleteTransaction = {},
            onEditTransaction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenDarkPreview() {
    FinexisTheme(darkTheme = true) {
        HistoryScreenContent(
            searchQuery = "",
            onSearchQueryChange = {},
            selectedFilter = "All",
            onFilterSelect = {},
            selectedDateFilter = "All Time",
            onDateFilterSelect = {},
            selectedCategoryFilter = "All",
            onCategoryFilterSelect = {},
            expenses = listOf(
                Expense(id = "1", amount = 1390.0, category = "Transport", paymentMethod = "Card", description = "Daily commute", isIncome = false),
                Expense(id = "2", amount = 27800.0, category = "Salary", paymentMethod = "Cash", description = "Monthly salary payout", isIncome = true),
                Expense(id = "3", amount = 50.0, category = "Food", paymentMethod = "Cash", description = "Coffee", isIncome = false)
            ),
            currency = "USD",
            onDeleteTransaction = {},
            onEditTransaction = {}
        )
    }
}
