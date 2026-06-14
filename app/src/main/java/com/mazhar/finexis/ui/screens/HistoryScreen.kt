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
    viewModel: ExpenseViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    HistoryScreenContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedFilter = selectedFilter,
        onFilterSelect = { selectedFilter = it },
        expenses = expenses,
        onDeleteTransaction = { id -> viewModel.deleteExpense(id) },
        onEditTransaction = { expense -> expenseToEdit = expense },
        modifier = modifier
    )

    if (expenseToEdit != null) {
        ExpenseDialog(
            expenseToEdit = expenseToEdit,
            onDismiss = { expenseToEdit = null },
            onConfirm = { amount, category, date, paymentMethod, description, isIncome ->
                viewModel.editExpense(
                    id = expenseToEdit!!.id,
                    amount = amount,
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
    expenses: List<Expense>,
    onDeleteTransaction: (String) -> Unit,
    onEditTransaction: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Filter Pills
        FadeInSlideUp(delayMillis = 100) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { onFilterSelect(filter) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = filter,
                            color = textColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Transactions list
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filteredExpenses = expenses.filter {
                val matchesSearch = it.category.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                val matchesFilter = when (selectedFilter) {
                    "Expense" -> !it.isIncome
                    "Income" -> it.isIncome
                    else -> true
                }
                matchesSearch && matchesFilter
            }

            filteredExpenses.forEachIndexed { index, expense ->
                StaggeredItem(index = index + 3) {
                    HistoryTransactionRow(
                        expense = expense,
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
                    text = if (expense.isIncome) "+Rs ${String.format("%.2f", expense.amount)}" else "-Rs ${String.format("%.2f", expense.amount)}",
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
            expenses = listOf(
                Expense(id = "1", amount = 1390.0, category = "Transport", paymentMethod = "Card", description = "Daily commute", isIncome = false),
                Expense(id = "2", amount = 27800.0, category = "Salary", paymentMethod = "Cash", description = "Monthly salary payout", isIncome = true),
                Expense(id = "3", amount = 50.0, category = "Food", paymentMethod = "Cash", description = "Coffee", isIncome = false)
            ),
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
            expenses = listOf(
                Expense(id = "1", amount = 1390.0, category = "Transport", paymentMethod = "Card", description = "Daily commute", isIncome = false),
                Expense(id = "2", amount = 27800.0, category = "Salary", paymentMethod = "Cash", description = "Monthly salary payout", isIncome = true),
                Expense(id = "3", amount = 50.0, category = "Food", paymentMethod = "Cash", description = "Coffee", isIncome = false)
            ),
            onDeleteTransaction = {},
            onEditTransaction = {}
        )
    }
}
