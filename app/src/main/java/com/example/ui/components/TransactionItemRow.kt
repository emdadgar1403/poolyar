package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.model.ExpenseCategory
import com.example.model.TransactionType
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.util.PersianDateUtil

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == TransactionType.EXPENSE.name
    val isAllowance = transaction.type == TransactionType.ALLOWANCE.name || transaction.type == TransactionType.INCOME.name
    val isSavingDeposit = transaction.type == TransactionType.SAVING_DEPOSIT.name
    val isGoal = transaction.type == TransactionType.GOAL_ALLOCATION.name

    val category = ExpenseCategory.fromName(transaction.category)

    val iconText = when {
        isAllowance -> "🎁"
        isSavingDeposit -> "🪙"
        isGoal -> "🎯"
        else -> category.emoji
    }

    val iconBgColor = when {
        isAllowance -> PrimaryTeal.copy(alpha = 0.14f)
        isSavingDeposit -> SuccessGreen.copy(alpha = 0.14f)
        isGoal -> Color(0xFFF59E0B).copy(alpha = 0.14f)
        else -> category.color.copy(alpha = 0.14f)
    }

    val amountColor = when {
        isAllowance -> SuccessGreen
        isExpense -> Color(0xFFEF4444)
        isSavingDeposit -> PrimaryTeal
        else -> MaterialTheme.colorScheme.onSurface
    }

    val amountPrefix = when {
        isAllowance -> "+"
        isExpense -> "-"
        isSavingDeposit -> "🪙"
        else -> ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right side: Icon + Title + Date
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = transaction.persianDate.ifEmpty { "همین امروز" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = transaction.note,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }

            // Left side: Amount + optional Delete button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$amountPrefix ${PersianDateUtil.formatTomans(transaction.amount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor
                    )

                    if (isExpense) {
                        Text(
                            text = category.titlePersian,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = category.color
                        )
                    }
                }

                if (onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "حذف تراکنش",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
