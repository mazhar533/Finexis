package com.mazhar.finexis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.mazhar.finexis.ui.theme.FinexisPrimary
import com.mazhar.finexis.ui.theme.FinexisTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mazhar.finexis.viewmodel.ExpenseViewModel
import com.mazhar.finexis.viewmodel.BudgetViewModel
import com.mazhar.finexis.viewmodel.PreferenceViewModel
import com.mazhar.finexis.ui.components.ExpenseDialog
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints

data class NavigationItem(
    val label: String,
    val iconResId: Int
)

@Composable
fun MainContainerScreen(
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    expenseViewModel: ExpenseViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel = viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    var showExpenseDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomBar(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = pagerState.currentPage == 0 || pagerState.currentPage == 2,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> HomeScreen(viewModel = expenseViewModel, budgetViewModel = budgetViewModel)
                    1 -> HistoryScreen(viewModel = expenseViewModel)
                    2 -> AnalyticsScreen(viewModel = expenseViewModel)
                    3 -> BudgetScreen(viewModel = expenseViewModel, budgetViewModel = budgetViewModel)
                    4 -> ProfileScreen(onLogoutSuccess = onLogoutSuccess, preferenceViewModel = preferenceViewModel)
                }
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val totalWidth = maxWidth
            val tabCount = items.size
            val tabWidth = totalWidth / tabCount

            // Animate the offset of the indicator capsule
            val targetOffset = tabWidth * selectedTabIndex
            val animatedOffset by animateDpAsState(
                targetValue = targetOffset,
                animationSpec = spring(
                    dampingRatio = 0.75f, // Subtle spring bounce
                    stiffness = 120f // Much slower, premium and smooth sliding
                ),
                label = "BottomNavHighlight"
            )

            // 1. Sliding capsule indicator (60.dp height, capsule covering icon and text)
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .width(tabWidth)
                    .height(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp, start = 4.dp, end = 4.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(FinexisPrimary.copy(alpha = 0.15f))
                )
            }

            // 2. Row of icons and labels (60.dp height, icons top-aligned with 4.dp padding)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Top
            ) {
                items.forEachIndexed { index, item ->
                    val isActive = selectedTabIndex == index
                    val interactionSource = remember { MutableInteractionSource() }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onTabSelected(index) }
                            .weight(1f)
                            .height(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .height(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.label,
                                tint = if (isActive) FinexisPrimary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = if (isActive) FinexisPrimary else MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
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
    val mockExpenseViewModel = remember { ExpenseViewModel() }
    val mockBudgetViewModel = remember { BudgetViewModel() }
    val mockPreferenceViewModel = remember { PreferenceViewModel(android.app.Application()) }

    FinexisTheme(darkTheme = false) {
        MainContainerScreen(
            onLogoutSuccess = {},
            expenseViewModel = mockExpenseViewModel,
            budgetViewModel = mockBudgetViewModel,
            preferenceViewModel = mockPreferenceViewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenDarkPreview() {
    val mockExpenseViewModel = remember { ExpenseViewModel() }
    val mockBudgetViewModel = remember { BudgetViewModel() }
    val mockPreferenceViewModel = remember { PreferenceViewModel(android.app.Application()) }

    FinexisTheme(darkTheme = true) {
        MainContainerScreen(
            onLogoutSuccess = {},
            expenseViewModel = mockExpenseViewModel,
            budgetViewModel = mockBudgetViewModel,
            preferenceViewModel = mockPreferenceViewModel
        )
    }
}
