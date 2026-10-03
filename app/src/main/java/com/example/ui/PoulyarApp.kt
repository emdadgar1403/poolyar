package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.dialogs.AddExpenseDialog
import com.example.ui.components.dialogs.AddFundsToGoalDialog
import com.example.ui.components.dialogs.AddGoalDialog
import com.example.ui.components.dialogs.ReceiveAllowanceDialog
import com.example.ui.components.dialogs.TransferSavingsDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.viewmodel.PoulyarViewModel

enum class PoulyarScreen(
    val titlePersian: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("خانه", Icons.Filled.Home, Icons.Outlined.Home),
    TRANSACTIONS("تراکنش‌ها", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    GOALS("اهداف", Icons.Filled.Flag, Icons.Outlined.Flag),
    REPORTS("گزارش‌ها", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("تنظیمات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun PoulyarApp(
    viewModel: PoulyarViewModel
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(PoulyarScreen.DASHBOARD) }

    // Dialog states
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showReceiveAllowanceDialog by remember { mutableStateOf(false) }
    var showTransferSavingsDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalForAddingFunds by remember { mutableStateOf<GoalEntity?>(null) }

    // Request Notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Back handling: pop to dashboard first before exiting
    BackHandler(enabled = currentScreen != PoulyarScreen.DASHBOARD) {
        currentScreen = PoulyarScreen.DASHBOARD
    }

    // Collect states
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val allWeeks by viewModel.allWeeks.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val monthlySummary by viewModel.monthlySummary.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                PoulyarScreen.entries.forEach { screen ->
                    val isSelected = screen == currentScreen
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.titlePersian
                            )
                        },
                        label = {
                            Text(
                                text = screen.titlePersian,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            PoulyarScreen.DASHBOARD -> {
                DashboardScreen(
                    state = dashboardState,
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onReceiveAllowanceClick = { showReceiveAllowanceDialog = true },
                    onTransferSavingsClick = { showTransferSavingsDialog = true },
                    onAddGoalClick = { showAddGoalDialog = true },
                    onViewAllTransactionsClick = { currentScreen = PoulyarScreen.TRANSACTIONS },
                    onDeleteTransactionClick = { viewModel.deleteTransaction(it) },
                    modifier = screenModifier
                )
            }
            PoulyarScreen.TRANSACTIONS -> {
                TransactionsScreen(
                    transactions = allTransactions,
                    onAddExpenseClick = { showAddExpenseDialog = true },
                    onDeleteTransactionClick = { viewModel.deleteTransaction(it) },
                    modifier = screenModifier
                )
            }
            PoulyarScreen.GOALS -> {
                GoalsScreen(
                    goals = allGoals,
                    onAddGoalClick = { showAddGoalDialog = true },
                    onAddFundsToGoalClick = { goalForAddingFunds = it },
                    onDeleteGoalClick = { viewModel.deleteGoal(it) },
                    modifier = screenModifier
                )
            }
            PoulyarScreen.REPORTS -> {
                ReportsScreen(
                    weeklySummary = weeklySummary,
                    monthlySummary = monthlySummary,
                    allWeeks = allWeeks,
                    modifier = screenModifier
                )
            }
            PoulyarScreen.SETTINGS -> {
                SettingsScreen(
                    settings = settings,
                    onSaveSettings = { viewModel.updateSettings(it) },
                    onSendTestNotification = { viewModel.sendTestNotification(context) },
                    onResetSampleData = { viewModel.resetToSampleData() },
                    onClearAllData = { viewModel.clearAllData() },
                    modifier = screenModifier
                )
            }
        }
    }

    // Dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, amount, category, note ->
                viewModel.recordExpense(title, amount, category, note)
            }
        )
    }

    if (showReceiveAllowanceDialog) {
        ReceiveAllowanceDialog(
            currentWeekNumber = dashboardState.currentWeekNumber,
            currentYear = dashboardState.currentYear,
            defaultAmount = dashboardState.weeklyAllowance,
            onDismiss = { showReceiveAllowanceDialog = false },
            onConfirm = { weekNum, year, amount, note ->
                viewModel.recordAllowancePayment(weekNum, year, amount, note)
            }
        )
    }

    if (showTransferSavingsDialog) {
        TransferSavingsDialog(
            availableBalance = dashboardState.currentBalance,
            totalSavings = dashboardState.totalSavings,
            onDismiss = { showTransferSavingsDialog = false },
            onDepositToSavings = { amount, note ->
                viewModel.transferToSavings(amount, note)
            },
            onWithdrawFromSavings = { amount, note ->
                viewModel.withdrawFromSavings(amount, note)
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, targetAmount, initialSaved, emoji ->
                viewModel.addGoal(title, targetAmount, initialSaved, emoji)
            }
        )
    }

    goalForAddingFunds?.let { goal ->
        AddFundsToGoalDialog(
            goal = goal,
            onDismiss = { goalForAddingFunds = null },
            onConfirm = { amount ->
                viewModel.addFundsToGoal(goal.id, amount)
            }
        )
    }
}
