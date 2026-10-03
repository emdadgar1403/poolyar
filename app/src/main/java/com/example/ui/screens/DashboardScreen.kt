package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DashboardState
import com.example.ui.components.ExpenseCategoryChart
import com.example.ui.components.PocketMoneyCard
import com.example.ui.components.PoulyarHeaderCard
import com.example.ui.components.QuickActionButtons
import com.example.ui.components.SpendingLimitBanner
import com.example.ui.components.TransactionItemRow

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAddExpenseClick: () -> Unit,
    onReceiveAllowanceClick: () -> Unit,
    onTransferSavingsClick: () -> Unit,
    onAddGoalClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onDeleteTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Header with Available Balance, Total Savings, Total Wealth
            PoulyarHeaderCard(state = state)
        }

        // Friendly spending warning banner if budget exceeded
        if (state.isExpenseLimitExceeded) {
            item {
                SpendingLimitBanner(
                    weeklyExpenses = state.weeklyExpenses,
                    weeklyLimit = state.weeklyExpenseLimit
                )
            }
        }

        // Big Weekly Pocket Money Card
        item {
            PocketMoneyCard(
                state = state,
                onReceiveAllowanceClick = onReceiveAllowanceClick
            )
        }

        // Quick Actions Row
        item {
            QuickActionButtons(
                onAddExpenseClick = onAddExpenseClick,
                onReceiveAllowanceClick = onReceiveAllowanceClick,
                onTransferSavingsClick = onTransferSavingsClick,
                onAddGoalClick = onAddGoalClick
            )
        }

        // Persian Cultural Hero Banner
        item {
            com.example.ui.components.PersianCulturalBanner()
        }

        // Section: "این هفته چطور خرج کردم؟" with Chart and Category amounts
        item {
            ExpenseCategoryChart(
                categoryExpenses = state.categoryExpensesThisWeek,
                title = "این هفته چطور خرج کردم؟"
            )
        }

        // Section: Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخرین تراکنش‌ها",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onViewAllTransactionsClick,
                    modifier = Modifier.testTag("view_all_transactions_button")
                ) {
                    Text(
                        text = "مشاهده همه",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Recent Transactions List
        if (state.recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هنوز تراکنشی ثبت نشده است.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = state.recentTransactions,
                key = { it.id }
            ) { tx ->
                TransactionItemRow(
                    transaction = tx,
                    onDeleteClick = { onDeleteTransactionClick(tx.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
