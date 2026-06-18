package com.mazhar.finexis.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mazhar.finexis.R
import com.mazhar.finexis.ui.theme.*
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.mazhar.finexis.ui.components.FadeInSlideUp
import com.mazhar.finexis.ui.components.StaggeredItem
import com.mazhar.finexis.ui.components.FinexisToast
import com.mazhar.finexis.model.Expense
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.math.roundToInt

data class CategorySpending(
    val name: String,
    val amount: Double,
    val color: Color
)

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel(),
    currency: String = "PKR",
    onChartPositioned: (Rect) -> Unit = {}
) {
    val expenses by viewModel.expenses.collectAsState()
    val context = LocalContext.current

    val totalIncome = expenses.filter { it.isIncome }.sumOf { it.amount }
    val totalExpenses = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val savings = totalIncome - totalExpenses

    val months = remember {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("MMM", Locale.US)
        for (i in 5 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            list.add(sdf.format(c.time))
        }
        list
    }

    val monthsAgo = remember {
        val list = mutableListOf<Int>()
        for (i in 5 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            list.add(c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH))
        }
        list
    }

    val monthlyValues = remember(expenses, monthsAgo) {
        val cal = Calendar.getInstance()
        val sums = FloatArray(6)
        expenses.forEach { expense ->
            if (!expense.isIncome) {
                cal.timeInMillis = expense.date
                val expenseMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
                val index = monthsAgo.indexOf(expenseMonth)
                if (index in 0..5) {
                    sums[index] += expense.amount.toFloat()
                }
            }
        }
        sums.toList()
    }

    val categorySpendings = remember(expenses) {
        listOf(
            CategorySpending("Food", expenses.filter { !it.isIncome && it.category.lowercase() == "food" }.sumOf { it.amount }, Color(0xFFF59E0B)),
            CategorySpending("Transport", expenses.filter { !it.isIncome && it.category.lowercase() == "transport" }.sumOf { it.amount }, Color(0xFF3B82F6)),
            CategorySpending("Shopping", expenses.filter { !it.isIncome && it.category.lowercase() == "shopping" }.sumOf { it.amount }, Color(0xFFEC4899)),
            CategorySpending("Other", expenses.filter { !it.isIncome && it.category.lowercase() == "other" }.sumOf { it.amount }, Color(0xFF8B5CF6))
        ).filter { it.amount > 0 }.sortedByDescending { it.amount }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isToastError by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AnalyticsScreenContent(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            savings = savings,
            months = months,
            monthsAgo = monthsAgo,
            monthlyValues = monthlyValues,
            categories = categorySpendings,
            expenses = expenses,
            currency = currency,
            modifier = Modifier.fillMaxSize(),
            onExportPdf = {
                com.mazhar.finexis.ui.utils.PdfExportHelper.exportTransactionsToPdf(
                    context = context,
                    expenses = expenses,
                    currency = currency,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    savings = savings,
                    onSuccess = {
                        toastMessage = "PDF statement exported successfully!"
                        isToastError = false
                    },
                    onError = { err ->
                        toastMessage = err
                        isToastError = true
                    }
                )
            },
            onChartPositioned = onChartPositioned
        )

        FinexisToast(
            message = toastMessage ?: "",
            visible = toastMessage != null,
            isError = isToastError,
            onDismiss = { toastMessage = null }
        )
    }
}

@Composable
fun AnalyticsScreenContent(
    totalIncome: Double,
    totalExpenses: Double,
    savings: Double,
    months: List<String>,
    monthsAgo: List<Int>,
    monthlyValues: List<Float>,
    categories: List<CategorySpending>,
    expenses: List<Expense>,
    currency: String,
    modifier: Modifier = Modifier,
    onExportPdf: () -> Unit = {},
    onChartPositioned: (Rect) -> Unit = {}
) {
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) } // Start with no selection
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    val selectedMonthExpenses = remember(expenses, selectedMonthIndex, monthsAgo) {
        if (selectedMonthIndex == null) emptyList()
        else {
            val cal = Calendar.getInstance()
            expenses.filter { expense ->
                if (expense.isIncome) return@filter false
                cal.timeInMillis = expense.date
                val expenseMonth = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
                monthsAgo.getOrNull(selectedMonthIndex!!) == expenseMonth
            }.sortedByDescending { it.date }
        }
    }

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
                    text = "Analytics",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Export PDF Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE0F2F1)) // Very light green/blue background
                        .clickable { onExportPdf() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_download),
                            contentDescription = "Export PDF",
                            tint = Color(0xFF00796B), // dark teal text
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export PDF",
                            color = Color(0xFF00796B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Financial Overview Cards (Income, Expenses, Savings)
        FadeInSlideUp(delayMillis = 100) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val cardWeight = 1f
                // Income Card
                Card(
                    modifier = Modifier.weight(cardWeight),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(totalIncome, currency, showDecimal = false),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinexisIncome
                        )
                    }
                }

                // Expenses Card
                Card(
                    modifier = Modifier.weight(cardWeight),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(totalExpenses, currency, showDecimal = false),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinexisExpense
                        )
                    }
                }

                // Savings Card
                Card(
                    modifier = Modifier.weight(cardWeight),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Savings", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(savings, currency, showDecimal = false),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00796B)
                        )
                    }
                }
            }
        }

        // Monthly Spending Trends Chart Card
        FadeInSlideUp(delayMillis = 200) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.positionInRoot()
                        val size = layoutCoordinates.size
                        onChartPositioned(Rect(position.x, position.y, position.x + size.width, position.y + size.height))
                    }
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Monthly Spending Trends",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val currentPeriodSpent = selectedMonthIndex?.let { monthlyValues.getOrNull(it)?.toInt() } ?: totalExpenses.toInt()
                    Text(
                        text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(currentPeriodSpent.toDouble(), currency, showDecimal = false),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom Bar Chart for months
                    SimpleBarChart(
                        days = months,
                        values = monthlyValues,
                        selectedIndex = selectedMonthIndex,
                        currency = currency,
                        onBarClick = { idx ->
                            selectedMonthIndex = if (selectedMonthIndex == idx) null else idx
                        }
                    )
                }
            }
        }

        // Dynamic Monthly Transactions Details Card (Shown inline below chart)
        selectedMonthIndex?.let { monthIdx ->
            val monthName = months.getOrNull(monthIdx) ?: ""
            val totalMonthSpent = monthlyValues.getOrNull(monthIdx)?.toInt() ?: 0

            FadeInSlideUp(delayMillis = 250) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transactions in $monthName",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total: " + com.mazhar.finexis.ui.utils.CurrencyHelper.format(totalMonthSpent.toDouble(), currency, showDecimal = false),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinexisExpense
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedMonthExpenses.isEmpty()) {
                            Text(
                                text = "No transactions in this month",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
                            selectedMonthExpenses.forEachIndexed { idx, expense ->
                                if (idx > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = expense.description.ifEmpty { expense.category },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = expense.paymentMethod,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = " • ",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = sdf.format(Date(expense.date)),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(expense.amount, currency, showDecimal = false),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinexisExpense
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Categories Allocation Card
        FadeInSlideUp(delayMillis = 300) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Category-wise Spending",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    if (categories.isEmpty()) {
                        Text(
                            text = "No spending data available",
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                    } else {
                        categories.forEachIndexed { index, cat ->
                            val isExpanded = expandedCategory == cat.name
                            val categoryExpenses = remember(expenses, cat.name) {
                                expenses.filter { !it.isIncome && it.category.equals(cat.name, ignoreCase = true) }
                                    .sortedByDescending { it.date }
                            }

                            val lastSpentDate = categoryExpenses.maxOfOrNull { it.date }
                            val formattedLastSpent = remember(lastSpentDate) {
                                lastSpentDate?.let {
                                    val sdf = SimpleDateFormat("MMM dd", Locale.US)
                                    "Last spent: ${sdf.format(Date(it))}"
                                } ?: "No transactions"
                            }

                            StaggeredItem(index = index + 4) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            expandedCategory = if (isExpanded) null else cat.name
                                        }
                                ) {
                                    if (index > 0) Spacer(modifier = Modifier.height(16.dp))
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
                                                    .background(cat.color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = cat.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = formattedLastSpent,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }

                                        val percentage = if (totalExpenses > 0) ((cat.amount / totalExpenses) * 100).toInt() else 0
                                        Text(
                                            text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(cat.amount, currency, showDecimal = false) + " (" + percentage + "%)",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val ratio = if (totalExpenses > 0) (cat.amount / totalExpenses).toFloat() else 0.0f
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = cat.color,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(12.dp)
                                        ) {
                                            categoryExpenses.forEachIndexed { idx, expense ->
                                                if (idx > 0) {
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 8.dp),
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = expense.description.ifEmpty { "Expense" },
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = expense.paymentMethod,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.secondary
                                                            )
                                                            Text(
                                                                text = " • ",
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.secondary
                                                            )
                                                            Text(
                                                                text = sdf.format(Date(expense.date)),
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.secondary
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = com.mazhar.finexis.ui.utils.CurrencyHelper.format(expense.amount, currency, showDecimal = false),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatAmountCompact(amount: Float, currency: String): String {
    val converted = com.mazhar.finexis.ui.utils.CurrencyHelper.convertPkrToActive(amount.toDouble(), currency).toFloat()
    val symbol = com.mazhar.finexis.ui.utils.CurrencyHelper.getSymbol(currency)
    return when {
        converted >= 1000000f -> {
            val formatted = String.format(Locale.US, "%.1f", converted / 1000000f)
            symbol + formatted.removeSuffix(".0") + "M"
        }
        converted >= 1000f -> {
            val formatted = String.format(Locale.US, "%.1f", converted / 1000f)
            symbol + formatted.removeSuffix(".0") + "k"
        }
        else -> symbol + converted.roundToInt().toString()
    }
}

@Composable
fun SimpleBarChart(
    days: List<String>,
    values: List<Float>,
    selectedIndex: Int?,
    currency: String,
    onBarClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. Bars & Tooltips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
            days.forEachIndexed { index, _ ->
                val value = values.getOrElse(index) { 0f }
                val fillHeightRatio = value / maxVal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onBarClick(index)
                        }
                ) {
                    if (index == selectedIndex) {
                        val tooltipOffset = when (index) {
                            0 -> 10.dp
                            days.lastIndex -> (-10).dp
                            else -> 0.dp
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = tooltipOffset)
                                .wrapContentWidth(unbounded = true)
                                .background(FinexisPrimary, shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = formatAmountCompact(value, currency),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    val isHighlighted = fillHeightRatio > 0.0f
                    val barColor = when {
                        index == selectedIndex -> FinexisPrimary
                        isHighlighted -> FinexisPrimary.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    }
                    val barHeight = if (isHighlighted) (110.dp * fillHeightRatio) else 10.dp

                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(barColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEachIndexed { index, day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onBarClick(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenLightPreview() {
    FinexisTheme(darkTheme = false) {
        AnalyticsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenDarkPreview() {
    FinexisTheme(darkTheme = true) {
        AnalyticsScreen()
    }
}

