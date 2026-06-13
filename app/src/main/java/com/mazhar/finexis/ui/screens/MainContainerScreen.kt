package com.mazhar.finexis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.mazhar.finexis.ui.theme.FinexisPrimary
import com.mazhar.finexis.ui.theme.FinexisTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import com.mazhar.finexis.viewmodel.BudgetViewModel
import com.mazhar.finexis.ui.components.ExpenseDialog

data class NavigationItem(
    val label: String,
    val iconResId: Int
)

@Composable
fun MainContainerScreen(
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    expenseViewModel: ExpenseViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel()
) {
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var showExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomBar(
                selectedTabIndex = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showExpenseDialog = true },
                containerColor = FinexisPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(viewModel = expenseViewModel, budgetViewModel = budgetViewModel)
                1 -> HistoryScreen(viewModel = expenseViewModel)
                2 -> AnalyticsScreen()
                3 -> BudgetScreen(viewModel = expenseViewModel, budgetViewModel = budgetViewModel)
                4 -> ProfileScreen(onLogoutSuccess = onLogoutSuccess)
            }
        }

        if (showExpenseDialog) {
            ExpenseDialog(
                onDismiss = { showExpenseDialog = false },
                onConfirm = { amount, category, date, paymentMethod, description, isIncome ->
                    expenseViewModel.addExpense(
                        amount = amount,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        description = description,
                        isIncome = isIncome
                    )
                    showExpenseDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomBottomBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = remember {
        listOf(
            NavigationItem("Home", R.drawable.icon_home),
            NavigationItem("History", R.drawable.icon_history),
            NavigationItem("Analytics", R.drawable.icon_analytics),
            NavigationItem("Budget", R.drawable.icon_budget),
            NavigationItem("Profile", R.drawable.icon_profile)
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isActive = selectedTabIndex == index
                if (isActive) {
                    // Active Pill Style
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(FinexisPrimary.copy(alpha = 0.15f))
                            .clickable { onTabSelected(index) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.label,
                                tint = FinexisPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.label,
                                color = FinexisPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Inactive Standard Style
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTabSelected(index) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconResId),
                            contentDescription = item.label,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenLightPreview() {
    FinexisTheme(darkTheme = false) {
        MainContainerScreen(onLogoutSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenDarkPreview() {
    FinexisTheme(darkTheme = true) {
        MainContainerScreen(onLogoutSuccess = {})
    }
}
