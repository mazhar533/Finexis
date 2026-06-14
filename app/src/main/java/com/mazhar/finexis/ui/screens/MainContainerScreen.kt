package com.mazhar.finexis.ui.screens

import android.annotation.SuppressLint
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
import com.mazhar.finexis.viewmodel.AuthViewModel
import com.mazhar.finexis.ui.components.ExpenseDialog
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NavigationItem(
    val label: String,
    val iconResId: Int
)

@SuppressLint("DefaultLocale")
@Composable
fun MainContainerScreen(
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    expenseViewModel: ExpenseViewModel = viewModel(),
    budgetViewModel: BudgetViewModel = viewModel(),
    preferenceViewModel: PreferenceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    var showExpenseDialog by remember { mutableStateOf(false) }

    val expenses by expenseViewModel.expenses.collectAsState()
    val budgetState by budgetViewModel.budget.collectAsState()
    val currency by preferenceViewModel.currency.collectAsState()
    val context = LocalContext.current

    val isBiometricEnabled by preferenceViewModel.isBiometricEnabled.collectAsState()
    var isAppUnlocked by rememberSaveable { mutableStateOf(false) }
    var lockScreenError by remember { mutableStateOf<String?>(null) }

    val triggerBiometricUnlock = {
        val activity = com.mazhar.finexis.ui.utils.BiometricHelper.findActivity(context)
        if (activity != null) {
            com.mazhar.finexis.ui.utils.BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Unlock Finexis",
                subtitle = "Authenticate using fingerprint or face unlock to access the app",
                onSuccess = {
                    isAppUnlocked = true
                    lockScreenError = null
                },
                onError = { err ->
                    lockScreenError = "Unlock failed: $err"
                }
            )
        } else {
            isAppUnlocked = true
        }
    }

    val shouldLock = isBiometricEnabled && !isAppUnlocked

    LaunchedEffect(shouldLock) {
        if (shouldLock) {
            triggerBiometricUnlock()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isBiometricEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (isBiometricEnabled) {
                    isAppUnlocked = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Keep track of already warned limits in memory for this session
    // to avoid multiple duplicate alerts on recompositions.
    var lastWarnedMonth by remember { mutableStateOf("") }
    var lastWarnedCategories by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(expenses, budgetState) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())

        // Calculate today's spent amount and transaction count
        // Filter out incomes (only count expenses)
        val todayCalendar = Calendar.getInstance()
        val todayExpenses = expenses.filter {
            if (it.isIncome) return@filter false
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
        }
        val todaySpent = todayExpenses.sumOf { it.amount }
        val todayCount = todayExpenses.size

        // Cache the daily spent totals in shared preferences for background worker access
        com.mazhar.finexis.notification.NotificationHelper.cacheDailySpentData(context, todaySpent, todayCount)

        // --- Budget Limit Alert Calculations ---
        val monthlyLimit = budgetState.monthlyLimit
        val totalSpent = expenses.filter { !it.isIncome }.sumOf { it.amount }

        if (monthlyLimit > 0) {
            val ratio = totalSpent / monthlyLimit
            val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

            if (ratio >= 1.0 && lastWarnedMonth != "${currentMonthStr}_100") {
                lastWarnedMonth = "${currentMonthStr}_100"
                com.mazhar.finexis.notification.NotificationHelper.showBudgetAlert(
                    context,
                    "Monthly Budget Exceeded! ⚠️",
                    "You have exceeded your monthly budget! Total spent: Rs ${String.format("%.2f", totalSpent)} / Rs ${String.format("%.2f", monthlyLimit)}"
                )
            } else if (ratio in 0.9..<1.0 && lastWarnedMonth != "${currentMonthStr}_90" && lastWarnedMonth != "${currentMonthStr}_100") {
                lastWarnedMonth = "${currentMonthStr}_90"
                com.mazhar.finexis.notification.NotificationHelper.showBudgetAlert(
                    context,
                    "Monthly Budget Warning! 🔔",
                    "You have used 90% of your monthly budget! Total spent: Rs ${String.format("%.2f", totalSpent)}"
                )
            }
        }

        // Check category-wise limits: Food, Transport, Shopping, Other
        val categories = listOf(
            Triple("Food", budgetState.foodLimit, expenses.filter { !it.isIncome && it.category.equals("Food", ignoreCase = true) }.sumOf { it.amount }),
            Triple("Transport", budgetState.transportLimit, expenses.filter { !it.isIncome && it.category.equals("Transport", ignoreCase = true) }.sumOf { it.amount }),
            Triple("Shopping", budgetState.shoppingLimit, expenses.filter { !it.isIncome && it.category.equals("Shopping", ignoreCase = true) }.sumOf { it.amount }),
            Triple("Other", budgetState.otherLimit, expenses.filter { !it.isIncome && it.category.equals("Other", ignoreCase = true) }.sumOf { it.amount })
        )

        val newWarnedCategories = lastWarnedCategories.toMutableSet()
        categories.forEach { (name, limit, spent) ->
            if (limit > 0) {
                val ratio = spent / limit
                val key100 = "${name}_100"
                val key90 = "${name}_90"

                if (ratio >= 1.0 && !lastWarnedCategories.contains(key100)) {
                    newWarnedCategories.add(key100)
                    com.mazhar.finexis.notification.NotificationHelper.showBudgetAlert(
                        context,
                        "Category Limit Exceeded! ⚠️",
                        "The $name category budget has been exceeded! Total spent: Rs ${String.format("%.2f", spent)} / Rs ${String.format("%.2f", limit)}"
                    )
                } else if (ratio >= 0.9 && ratio < 1.0 && !lastWarnedCategories.contains(key90) && !lastWarnedCategories.contains(key100)) {
                    newWarnedCategories.add(key90)
                    com.mazhar.finexis.notification.NotificationHelper.showBudgetAlert(
                        context,
                        "Category Limit Warning! 🔔",
                        "You have used 90% of your $name category budget! Total spent: Rs ${String.format("%.2f", spent)}"
                    )
                }
            }
        }
        if (newWarnedCategories != lastWarnedCategories) {
            lastWarnedCategories = newWarnedCategories
        }
    }

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
                    0 -> HomeScreen(
                        viewModel = expenseViewModel,
                        budgetViewModel = budgetViewModel,
                        authViewModel = authViewModel,
                        currency = currency,
                        onCurrencyChange = { newCurrency ->
                            preferenceViewModel.setCurrency(newCurrency)
                        },
                        onNavigateToSettings = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        }
                    )
                    1 -> HistoryScreen(viewModel = expenseViewModel, currency = currency)
                    2 -> AnalyticsScreen(viewModel = expenseViewModel, currency = currency)
                    3 -> BudgetScreen(viewModel = expenseViewModel, budgetViewModel = budgetViewModel, currency = currency)
                    4 -> ProfileScreen(onLogoutSuccess = onLogoutSuccess, preferenceViewModel = preferenceViewModel, authViewModel = authViewModel)
                }
            }
        }

        if (showExpenseDialog) {
            ExpenseDialog(
                currency = currency,
                onDismiss = { showExpenseDialog = false },
                onConfirm = { amount, category, date, paymentMethod, description, isIncome ->
                    val amountInPkr = com.mazhar.finexis.ui.utils.CurrencyHelper.convertActiveToPkr(amount, currency)
                    expenseViewModel.addExpense(
                        amount = amountInPkr,
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

        if (shouldLock) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Consume clicks to lock underlying screens
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { triggerBiometricUnlock() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_biomatric),
                            contentDescription = "Unlock app icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Finexis is Locked",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Authenticate using Touch ID to access your account",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    if (lockScreenError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = lockScreenError!!,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { triggerBiometricUnlock() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Unlock",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            val activity = com.mazhar.finexis.ui.utils.BiometricHelper.findActivity(context)
                            activity?.finish()
                        }
                    ) {
                        Text(
                            text = "Exit App",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
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
    val mockAuthViewModel = remember { AuthViewModel() }

    FinexisTheme(darkTheme = false) {
        MainContainerScreen(
            onLogoutSuccess = {},
            expenseViewModel = mockExpenseViewModel,
            budgetViewModel = mockBudgetViewModel,
            preferenceViewModel = mockPreferenceViewModel,
            authViewModel = mockAuthViewModel
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenDarkPreview() {
    val mockExpenseViewModel = remember { ExpenseViewModel() }
    val mockBudgetViewModel = remember { BudgetViewModel() }
    val mockPreferenceViewModel = remember { PreferenceViewModel(android.app.Application()) }
    val mockAuthViewModel = remember { AuthViewModel() }

    FinexisTheme(darkTheme = true) {
        MainContainerScreen(
            onLogoutSuccess = {},
            expenseViewModel = mockExpenseViewModel,
            budgetViewModel = mockBudgetViewModel,
            preferenceViewModel = mockPreferenceViewModel,
            authViewModel = mockAuthViewModel
        )
    }
}
