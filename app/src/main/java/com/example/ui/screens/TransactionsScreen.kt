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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.model.TransactionType
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.PrimaryTeal
import com.example.util.PersianDateUtil

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    onAddExpenseClick: () -> Unit,
    onDeleteTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("همه", "هزینه‌ها 💳", "پول توجیبی 🎁", "پس‌انداز 🪙")

    val filteredTransactions = remember(selectedTabIndex, transactions) {
        when (selectedTabIndex) {
            1 -> transactions.filter { it.type == TransactionType.EXPENSE.name }
            2 -> transactions.filter { it.type == TransactionType.ALLOWANCE.name || it.type == TransactionType.INCOME.name }
            3 -> transactions.filter { it.type == TransactionType.SAVING_DEPOSIT.name || it.type == TransactionType.SAVING_WITHDRAW.name }
            else -> transactions
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("transactions_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "دفترچه تراکنش‌ها 📋",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ثبت شفاف درآمدها، هزینه‌ها و پس‌اندازهای تو",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                divider = {},
                modifier = Modifier.fillMaxWidth().testTag("transaction_filter_tabs")
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Count summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تعداد: ${PersianDateUtil.toPersianDigits(filteredTransactions.size.toString())} تراکنش",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val totalAmount = filteredTransactions.sumOf { it.amount }
                Text(
                    text = "مجموع: ${PersianDateUtil.formatTomans(totalAmount)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تراکنشی در این دسته‌بندی یافت نشد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredTransactions,
                        key = { it.id }
                    ) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            onDeleteClick = { onDeleteTransactionClick(tx.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddExpenseClick,
            containerColor = PrimaryTeal,
            contentColor = androidx.compose.ui.graphics.Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("transactions_add_expense_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "ثبت هزینه جدید"
            )
        }
    }
}
