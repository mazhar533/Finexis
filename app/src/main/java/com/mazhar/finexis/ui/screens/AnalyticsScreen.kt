package com.mazhar.finexis.ui.screens

import androidx.compose.foundation.background
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
import java.util.Calendar

data class CategorySpending(
    val name: String,
    val amount: Double,
    val color: Color
)

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsState()

    val totalSpending = expenses.filter { !it.isIncome }.sumOf { it.amount }
    val days = remember { listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat") }

    val values = remember(expenses) {
        val calendar = Calendar.getInstance()
        val dailySums = FloatArray(7)
        expenses.forEach { expense ->
            if (!expense.isIncome) {
                calendar.timeInMillis = expense.date
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val index = dayOfWeek - 1
                if (index in 0..6) {
                    dailySums[index] += expense.amount.toFloat()
                }
            }
        }
        dailySums.toList()
    }

    val categorySpendings = remember(expenses) {
        listOf(
            CategorySpending("Food", expenses.filter { !it.isIncome && it.category.lowercase() == "food" }.sumOf { it.amount }, Color(0xFFF59E0B)),
            CategorySpending("Transport", expenses.filter { !it.isIncome && it.category.lowercase() == "transport" }.sumOf { it.amount }, Color(0xFF3B82F6)),
            CategorySpending("Shopping", expenses.filter { !it.isIncome && it.category.lowercase() == "shopping" }.sumOf { it.amount }, Color(0xFFEC4899)),
            CategorySpending("Other", expenses.filter { !it.isIncome && it.category.lowercase() == "other" }.sumOf { it.amount }, Color(0xFF8B5CF6))
        ).filter { it.amount > 0 }.sortedByDescending { it.amount }
    }

    AnalyticsScreenContent(
        totalSpending = totalSpending,
        days = days,
        values = values,
        categories = categorySpendings,
        modifier = modifier,
        onExportPdf = {}
    )
}

@Composable
fun AnalyticsScreenContent(
    totalSpending: Double,
    days: List<String>,
    values: List<Float>,
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier,
    onExportPdf: () -> Unit = {}
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

        // Total Spending Chart Card
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
                    text = "Total Spending",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rs ${totalSpending.toInt()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Custom Bar Chart
                SimpleBarChart(days = days, values = values)
            }
        }

        // Top Categories Allocation Card
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
                    text = "Top Categories",
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
                                Text(
                                    text = cat.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Rs ${cat.amount.toInt()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val ratio = if (totalSpending > 0) (cat.amount / totalSpending).toFloat() else 0.0f
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = cat.color,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    days: List<String>,
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
        days.forEachIndexed { index, day ->
            val value = values.getOrElse(index) { 0f }
            val fillHeightRatio = value / maxVal

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Bar indicator: Highlight positive values (Fri), otherwise show empty placeholders
                val isHighlighted = fillHeightRatio > 0.0f
                val barColor = if (isHighlighted) FinexisPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                val barHeight = if (isHighlighted) (130.dp * fillHeightRatio) else 10.dp

                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(barColor)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
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
